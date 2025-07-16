package ktb.leafresh.backend.domain.feedback.infrastructure.publisher;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ktb.leafresh.backend.domain.feedback.domain.entity.FeedbackFailureLog;
import ktb.leafresh.backend.domain.feedback.infrastructure.dto.request.AiFeedbackCreationRequestDto;
import ktb.leafresh.backend.domain.feedback.infrastructure.repository.FeedbackFailureLogRepository;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
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
@Profile("eks")
@Slf4j
@RequiredArgsConstructor
public class AwsAiFeedbackSqsPublisher implements AiFeedbackPublisher {

    private final AmazonSQSAsync amazonSQSAsync;
    private final ObjectMapper objectMapper;
    private final MemberRepository memberRepository;
    private final FeedbackFailureLogRepository feedbackFailureLogRepository;

    @Value("${aws.sqs.feedback-request-queue-url}")
    private String queueUrl;

    private static final int MAX_RETRY = 3;
    private static final long INITIAL_BACKOFF_MS = 300;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    @Override
    public void publishAsyncWithRetry(AiFeedbackCreationRequestDto dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            sendWithRetry(json, dto, 1);
        } catch (JsonProcessingException e) {
            log.error("[SQS 직렬화 실패]", e);
            logFailure(dto, null, "직렬화 실패: " + e.getMessage());
        }
    }

    private void sendWithRetry(String body, AiFeedbackCreationRequestDto dto, int attempt) {
        try {
            amazonSQSAsync.sendMessage(queueUrl, body);
            log.info("[SQS 피드백 발행 성공] attempt={}, dto={}", attempt, body);
        } catch (Exception e) {
            log.warn("[SQS 피드백 발행 실패] attempt={}, error={}", attempt, e.getMessage());

            if (attempt < MAX_RETRY) {
                long backoff = INITIAL_BACKOFF_MS * (1L << (attempt - 1));
                scheduler.schedule(() -> sendWithRetry(body, dto, attempt + 1), backoff, TimeUnit.MILLISECONDS);
            } else {
                log.error("[SQS 피드백 발행 최종 실패] dto={}", dto);
                logFailure(dto, body, "최대 재시도 초과: " + e.getMessage());
            }
        }
    }

    private void logFailure(AiFeedbackCreationRequestDto dto, String json, String reason) {
        try {
            Member member = memberRepository.findById(dto.memberId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

            feedbackFailureLogRepository.save(FeedbackFailureLog.builder()
                    .member(member)
                    .reason(reason)
                    .requestBody(json != null ? json : "{}")
                    .occurredAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("[FailureLog 저장 실패] {}", e.getMessage());
        }
    }
}
