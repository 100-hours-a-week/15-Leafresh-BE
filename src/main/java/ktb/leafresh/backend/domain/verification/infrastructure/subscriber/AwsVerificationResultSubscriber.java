package ktb.leafresh.backend.domain.verification.infrastructure.subscriber;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.verification.application.service.VerificationResultProcessor;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.dto.request.AiVerificationRequestDto;
import ktb.leafresh.backend.domain.verification.infrastructure.publisher.AiVerificationPublisher;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.VerificationResultRequestDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Profile("eks")
@RequiredArgsConstructor
public class AwsVerificationResultSubscriber {

    private final AmazonSQSAsync sqs;
    private final ObjectMapper objectMapper;
    private final VerificationResultProcessor resultProcessor;
    private final GroupChallengeVerificationRepository verificationRepository;
    private final AiVerificationPublisher aiVerificationPublisher;

    @Value("${aws.sqs.verification-result-queue-url}")
    private String queueUrl;

    private static final int WAIT_TIME_SECONDS = 20;
    private static final int MAX_MESSAGES = 5;

    @PostConstruct
    public void startPolling() {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(this::pollMessages, 0, 5, TimeUnit.SECONDS);
        log.info("[SQS 인증 결과 Subscriber 시작] queueUrl={}", queueUrl);
    }

//    @Async
//    ScheduledExecutorService로 실행되므로 @Async는 효과 없음 => 제거해도 문제 X
    public void pollMessages() {
        try {
            ReceiveMessageRequest request = new ReceiveMessageRequest(queueUrl)
                    .withMaxNumberOfMessages(MAX_MESSAGES)
                    .withWaitTimeSeconds(WAIT_TIME_SECONDS);

            List<Message> messages = sqs.receiveMessage(request).getMessages();

            for (Message message : messages) {
                String body = message.getBody();
                log.info("[SQS 인증 결과 수신] messageId={}, body={}", message.getMessageId(), body);

                try {
                    VerificationResultRequestDto dto = objectMapper.readValue(body, VerificationResultRequestDto.class);

                    if (dto.isSuccessResult()) {
                        resultProcessor.process(dto.verificationId(), dto);

                    } else if (dto.isRecoverableHttpError()) {
                        log.warn("[AI 처리 오류 응답] verificationId={}, httpStatus={}", dto.verificationId(), dto.result());

                        GroupChallengeVerification verification = verificationRepository.findById(dto.verificationId())
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 인증 ID"));

                        GroupChallenge challenge = verification.getParticipantRecord().getGroupChallenge();

                        AiVerificationRequestDto retryDto = AiVerificationRequestDto.builder()
                                .verificationId(dto.verificationId())
                                .type(dto.type())
                                .imageUrl(verification.getImageUrl())
                                .memberId(dto.memberId())
                                .challengeId(dto.challengeId())
                                .date(dto.date())
                                .challengeName(challenge.getTitle())
                                .challengeInfo(challenge.getDescription())
                                .build();

                        aiVerificationPublisher.publishAsyncWithRetry(retryDto);
                        log.info("[SQS 인증 요청 재발행 완료] verificationId={}", dto.verificationId());

                    } else {
                        log.error("[알 수 없는 result 값] result={}", dto.result());
                    }

                    // 삭제
                    sqs.deleteMessage(new DeleteMessageRequest(queueUrl, message.getReceiptHandle()));

                } catch (CustomException e) {
                    if (e.getErrorCode() == VerificationErrorCode.VERIFICATION_NOT_FOUND) {
                        log.warn("[무시] 존재하지 않는 인증 ID. verificationId={} → 삭제", e.getMessage());
                        sqs.deleteMessage(new DeleteMessageRequest(queueUrl, message.getReceiptHandle()));
                    } else {
                        log.error("[처리 실패 - CustomException] {}", e.getMessage(), e);
                        // 삭제하지 않음 (재시도 유도)
                    }
                } catch (Exception e) {
                    log.error("[처리 실패 - Exception] {}", e.getMessage(), e);
                    // 삭제하지 않음 (재시도 유도)
                }
            }
        } catch (Exception e) {
            log.error("[SQS Polling 실패] {}", e.getMessage(), e);
        }
    }
}
