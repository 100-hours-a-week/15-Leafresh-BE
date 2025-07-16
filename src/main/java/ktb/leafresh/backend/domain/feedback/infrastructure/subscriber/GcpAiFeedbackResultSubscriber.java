package ktb.leafresh.backend.domain.feedback.infrastructure.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import jakarta.annotation.PostConstruct;
import ktb.leafresh.backend.domain.feedback.application.assembler.FeedbackDtoAssembler;
import ktb.leafresh.backend.domain.feedback.application.service.FeedbackResultService;
import ktb.leafresh.backend.domain.feedback.infrastructure.dto.request.AiFeedbackCreationRequestDto;
import ktb.leafresh.backend.domain.feedback.infrastructure.publisher.GcpAiFeedbackPubSubPublisher;
import ktb.leafresh.backend.domain.feedback.presentation.dto.request.FeedbackResultRequestDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.FeedbackErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!eks")
public class GcpAiFeedbackResultSubscriber {

    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final FeedbackResultService feedbackResultService;
    private final GcpAiFeedbackPubSubPublisher pubSubPublisher;
    private final FeedbackDtoAssembler feedbackDtoAssembler;

    @PostConstruct
    public void subscribe() {
        String projectId = environment.getProperty("gcp.project-id");
        String subscriptionId = environment.getProperty("gcp.pubsub.subscriptions.feedback-result");

        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);

        MessageReceiver receiver = (message, consumer) -> {
            String rawData = message.getData().toStringUtf8();
            log.info("[피드백 결과 수신] messageId={}, data={}", message.getMessageId(), rawData);

            try {
                FeedbackResultRequestDto dto = objectMapper.readValue(rawData, FeedbackResultRequestDto.class);

                if (dto.isSuccessResult()) {
                    feedbackResultService.receiveFeedback(dto);

                } else if (dto.isRecoverableHttpError()) {
                    log.warn("[AI 처리 오류 응답] memberId={}, httpStatus={}", dto.memberId(), dto.content());

                    // 지난 주 월~일 기준으로 피드백 요청 DTO 재생성
                    LocalDate monday = getLastWeekStart();
                    LocalDate sunday = monday.plusDays(6);
                    AiFeedbackCreationRequestDto retryDto =
                            feedbackDtoAssembler.assemble(dto.memberId(), monday, sunday);

                    // 비동기 방식으로 재발행
                    pubSubPublisher.publishAsyncWithRetry(retryDto);
                    log.info("[AI 피드백 재발행 요청 전송] memberId={}, monday={}, sunday={}",
                            dto.memberId(), monday, sunday);

                } else {
                    log.error("[알 수 없는 content 값] content={}, memberId={}", dto.content(), dto.memberId());
                }

                consumer.ack();

            } catch (CustomException e) {
                if (e.getErrorCode() == FeedbackErrorCode.ALREADY_FEEDBACK_EXISTS) {
                    log.warn("[중복된 피드백 응답] memberId={}, 무시하고 ack", e.getMessage());
                    consumer.ack(); // 무시하고 ack 처리 (테스트 용도로 AI 서버에서 id가 보낼 수 있기 때문)
                } else {
                    log.error("[피드백 결과 메시지 처리 실패 - CustomException] {}", e.getMessage(), e);
                    consumer.nack(); // 다른 에러는 nack
                }
            } catch (Exception e) {
                log.error("[피드백 결과 메시지 처리 실패 - 기타 Exception]", e);
                consumer.nack();
            }
        };

        Subscriber subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
        subscriber.startAsync().awaitRunning();
        log.info("[피드백 결과 메시지 구독 시작]");
    }

    private LocalDate getLastWeekStart() {
        return LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1);
    }
}
