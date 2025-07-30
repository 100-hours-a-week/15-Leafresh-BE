package ktb.leafresh.backend.domain.verification.infrastructure.publisher;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.verification.domain.entity.VerificationFailureLog;
import ktb.leafresh.backend.domain.verification.infrastructure.dto.request.AiVerificationRequestDto;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.VerificationFailureLogRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@Profile("eks")
@RequiredArgsConstructor
public class AwsAiVerificationSqsPublisher implements AiVerificationPublisher {

    private final AmazonSQSAsync amazonSQSAsync;
    private final ObjectMapper objectMapper;
    private final MemberRepository memberRepository;
    private final VerificationFailureLogRepository failureLogRepository;

    @Value("${aws.sqs.verification-request-queue-url}")
    private String queueUrl;

    private static final int MAX_RETRY = 3;
    private static final long INITIAL_BACKOFF_MS = 300;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    @Override
    public void publishAsyncWithRetry(AiVerificationRequestDto dto) {
        log.info("[디버그] publishAsyncWithRetry 진입, dto={}", dto);

        try {
            String json = objectMapper.writeValueAsString(dto);
            log.info("[디버그] 직렬화 성공: {}", json);
            log.info("[디버그] queueUrl={}", queueUrl);
            sendWithRetry(json, dto, 1);
        } catch (JsonProcessingException e) {
            log.error("[AI 인증 직렬화 실패]", e);
            logFailure(dto, null, "직렬화 실패: " + e.getMessage());
        }
    }

    private void sendWithRetry(String body, AiVerificationRequestDto dto, int attempt) {
        try {
            amazonSQSAsync.sendMessage(queueUrl, body);
            log.info("[SQS 인증 요청 발행 성공] attempt={}, dto={}", attempt, body);
        } catch (Exception e) {
            log.warn("[SQS 인증 요청 발행 실패] attempt={}, error={}", attempt, e.getMessage());

            if (attempt < MAX_RETRY) {
                long backoff = INITIAL_BACKOFF_MS * (1L << (attempt - 1));
                scheduler.schedule(() -> sendWithRetry(body, dto, attempt + 1), backoff, TimeUnit.MILLISECONDS);
            } else {
                log.error("[SQS 인증 발행 최종 실패] dto={}, error={}", body, e.getMessage());
                logFailure(dto, body, "최대 재시도 초과: " + e.getMessage());
            }
        }
    }

    private void logFailure(AiVerificationRequestDto dto, String json, String reason) {
        try {
            Member member = memberRepository.findById(dto.memberId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

            failureLogRepository.save(VerificationFailureLog.builder()
                    .member(member)
                    .challengeType(dto.type() != null ? dto.type() : ChallengeType.GROUP)
                    .challengeId(dto.challengeId())
                    .verificationId(dto.verificationId())
                    .reason(reason)
                    .requestBody(json != null ? json : "{}")
                    .occurredAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("[FailureLog 저장 실패] {}", e.getMessage());
        }
    }
}
