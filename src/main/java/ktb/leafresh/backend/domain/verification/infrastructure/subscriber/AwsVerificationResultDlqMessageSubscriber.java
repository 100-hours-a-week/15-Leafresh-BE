package ktb.leafresh.backend.domain.verification.infrastructure.subscriber;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.verification.domain.entity.VerificationFailureLog;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.VerificationFailureLogRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.VerificationResultRequestDto;
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
public class AwsVerificationResultDlqMessageSubscriber {

    private final AmazonSQSAsync sqs;
    private final ObjectMapper objectMapper;
    private final VerificationFailureLogRepository failureLogRepository;
    private final MemberRepository memberRepository;

    @Value("${aws.sqs.verification-dlq-queue-url}")
    private String queueUrl;

    private static final int WAIT_TIME_SECONDS = 20;
    private static final int MAX_MESSAGES = 5;

    @PostConstruct
    public void startPolling() {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(this::pollMessages, 0, 5, TimeUnit.SECONDS);
        log.info("[SQS DLQ Subscriber 시작] queueUrl={}", queueUrl);
    }

    public void pollMessages() {
        try {
            ReceiveMessageRequest request = new ReceiveMessageRequest(queueUrl)
                    .withMaxNumberOfMessages(MAX_MESSAGES)
                    .withWaitTimeSeconds(WAIT_TIME_SECONDS);

            List<Message> messages = sqs.receiveMessage(request).getMessages();

            for (Message message : messages) {
                String body = message.getBody();
                log.error("[SQS DLQ 수신] messageId={}, body={}", message.getMessageId(), body);

                try {
                    VerificationResultRequestDto dto = objectMapper.readValue(body, VerificationResultRequestDto.class);

                    Member member = memberRepository.findById(dto.memberId())
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

                    failureLogRepository.save(VerificationFailureLog.builder()
                            .member(member)
                            .challengeType(dto.type())
                            .challengeId(dto.challengeId())
                            .verificationId(dto.verificationId())
                            .reason("DLQ로 이동된 인증 메시지입니다.")
                            .requestBody(body)
                            .occurredAt(LocalDateTime.now())
                            .build());

                } catch (Exception e) {
                    log.warn("[DLQ 메시지 파싱 실패 → 최소 정보로 로그 저장] {}", e.getMessage(), e);

                    failureLogRepository.save(VerificationFailureLog.builder()
                            .reason("DLQ 메시지 파싱 실패: " + e.getMessage())
                            .requestBody(body)
                            .occurredAt(LocalDateTime.now())
                            .build());
                } finally {
                    // DLQ는 무조건 ack
                    sqs.deleteMessage(new DeleteMessageRequest(queueUrl, message.getReceiptHandle()));
                }
            }
        } catch (Exception e) {
            log.error("[SQS DLQ Polling 실패] {}", e.getMessage(), e);
        }
    }
}
