package ktb.leafresh.backend.domain.feedback.infrastructure.subscriber;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import ktb.leafresh.backend.domain.feedback.application.assembler.FeedbackDtoAssembler;
import ktb.leafresh.backend.domain.feedback.application.service.FeedbackResultService;
import ktb.leafresh.backend.domain.feedback.infrastructure.dto.request.AiFeedbackCreationRequestDto;
import ktb.leafresh.backend.domain.feedback.infrastructure.publisher.AiFeedbackPublisher;
import ktb.leafresh.backend.domain.feedback.presentation.dto.request.FeedbackResultRequestDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.FeedbackErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Profile("eks")
@RequiredArgsConstructor
public class AwsAiFeedbackResultSubscriber {

    private final AmazonSQSAsync sqs;
    private final ObjectMapper objectMapper;
    private final FeedbackResultService feedbackResultService;
    private final AiFeedbackPublisher aiFeedbackPublisher;
    private final FeedbackDtoAssembler feedbackDtoAssembler;

    @Value("${aws.sqs.feedback-result-queue-url}")
    private String queueUrl;

    private static final int WAIT_TIME_SECONDS = 20;
    private static final int MAX_MESSAGES = 5;

    @PostConstruct
    public void startPolling() {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(this::pollMessages, 0, 5, TimeUnit.SECONDS);
        log.info("[SQS 피드백 결과 Subscriber 시작] queueUrl={}", queueUrl);
    }

    public void pollMessages() {
        try {
            ReceiveMessageRequest request = new ReceiveMessageRequest(queueUrl)
                    .withMaxNumberOfMessages(MAX_MESSAGES)
                    .withWaitTimeSeconds(WAIT_TIME_SECONDS);

            List<Message> messages = sqs.receiveMessage(request).getMessages();

            for (Message message : messages) {
                String body = message.getBody();
                log.info("[SQS 피드백 결과 수신] messageId={}, body={}", message.getMessageId(), body);

                try {
                    FeedbackResultRequestDto dto = objectMapper.readValue(body, FeedbackResultRequestDto.class);

                    if (dto.isSuccessResult()) {
                        feedbackResultService.receiveFeedback(dto);

                    } else if (dto.isRecoverableHttpError()) {
                        log.warn("[AI 처리 오류 응답] memberId={}, httpStatus={}", dto.memberId(), dto.content());

                        LocalDate monday = getLastWeekStart();
                        LocalDate sunday = monday.plusDays(6);

                        AiFeedbackCreationRequestDto retryDto =
                                feedbackDtoAssembler.assemble(dto.memberId(), monday, sunday);

                        aiFeedbackPublisher.publishAsyncWithRetry(retryDto);
                        log.info("[SQS 피드백 요청 재발행 완료] memberId={}, monday={}, sunday={}", dto.memberId(), monday, sunday);

                    } else {
                        log.error("[알 수 없는 content 값] content={}, memberId={}", dto.content(), dto.memberId());
                    }

                    sqs.deleteMessage(new DeleteMessageRequest(queueUrl, message.getReceiptHandle()));

                } catch (CustomException e) {
                    if (e.getErrorCode() == FeedbackErrorCode.ALREADY_FEEDBACK_EXISTS) {
                        log.warn("[중복된 피드백 응답] memberId={}, 무시하고 삭제", e.getMessage());
                        sqs.deleteMessage(new DeleteMessageRequest(queueUrl, message.getReceiptHandle()));
                    } else {
                        log.error("[처리 실패 - CustomException] {}", e.getMessage(), e);
                    }
                } catch (Exception e) {
                    log.error("[처리 실패 - Exception]", e);
                }
            }
        } catch (Exception e) {
            log.error("[SQS Polling 실패] {}", e.getMessage(), e);
        }
    }

    private LocalDate getLastWeekStart() {
        return LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1);
    }
}
