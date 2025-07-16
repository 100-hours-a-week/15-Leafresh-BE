package ktb.leafresh.backend.domain.feedback.infrastructure.subscriber;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import ktb.leafresh.backend.domain.feedback.domain.entity.FeedbackFailureLog;
import ktb.leafresh.backend.domain.feedback.infrastructure.repository.FeedbackFailureLogRepository;
import ktb.leafresh.backend.domain.feedback.presentation.dto.request.FeedbackResultRequestDto;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Profile("eks")
@RequiredArgsConstructor
public class AwsAiFeedbackResultDlqSubscriber {

    private final AmazonSQSAsync sqs;
    private final ObjectMapper objectMapper;
    private final FeedbackFailureLogRepository failureLogRepository;
    private final MemberRepository memberRepository;

    @Value("${aws.sqs.feedback-result-dlq-queue-url}")
    private String dlqUrl;

    private static final int WAIT_TIME_SECONDS = 20;
    private static final int MAX_MESSAGES = 5;

    @PostConstruct
    public void startDlqPolling() {
        Executors.newSingleThreadScheduledExecutor()
                .scheduleWithFixedDelay(this::pollDlqMessages, 0, 10, TimeUnit.SECONDS);
        log.info("[SQS 피드백 DLQ Subscriber 시작] dlqUrl={}", dlqUrl);
    }

    public void pollDlqMessages() {
        try {
            ReceiveMessageRequest request = new ReceiveMessageRequest(dlqUrl)
                    .withMaxNumberOfMessages(MAX_MESSAGES)
                    .withWaitTimeSeconds(WAIT_TIME_SECONDS);

            List<Message> messages = sqs.receiveMessage(request).getMessages();

            for (Message message : messages) {
                String body = message.getBody();
                log.error("[SQS 피드백 DLQ 수신] messageId={}, body={}", message.getMessageId(), body);

                try {
                    FeedbackResultRequestDto dto = objectMapper.readValue(body, FeedbackResultRequestDto.class);

                    Member member = memberRepository.findById(dto.memberId())
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

                    failureLogRepository.save(FeedbackFailureLog.builder()
                            .member(member)
                            .reason("DLQ로 이동된 피드백 메시지입니다.")
                            .requestBody(body)
                            .occurredAt(LocalDateTime.now())
                            .build());

                } catch (Exception e) {
                    log.warn("[DLQ 메시지 파싱 실패] 최소 정보로 로그 저장. error={}", e.getMessage());

                    failureLogRepository.save(FeedbackFailureLog.builder()
                            .reason("DLQ 메시지 파싱 실패: " + e.getMessage())
                            .requestBody(body)
                            .occurredAt(LocalDateTime.now())
                            .build());
                } finally {
                    sqs.deleteMessage(new DeleteMessageRequest(dlqUrl, message.getReceiptHandle()));
                }
            }

        } catch (Exception e) {
            log.error("[SQS DLQ Polling 실패] {}", e.getMessage(), e);
        }
    }
}
