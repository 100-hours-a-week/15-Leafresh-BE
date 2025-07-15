package ktb.leafresh.backend.domain.verification.infrastructure.publisher;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ktb.leafresh.backend.domain.verification.infrastructure.dto.request.AiVerificationRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Profile("eks")
@Slf4j
@RequiredArgsConstructor
public class AwsAiVerificationSqsPublisher implements AiVerificationPublisher {

    private final AmazonSQSAsync amazonSQSAsync;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.verification-request-queue-url}")
    private String queueUrl;

    private static final int MAX_RETRY = 3;
    private static final long INITIAL_BACKOFF_MS = 300;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    @Override
    public void publishAsyncWithRetry(AiVerificationRequestDto dto) {
        try {
            String messageBody = objectMapper.writeValueAsString(dto);
            sendWithRetry(messageBody, dto, 1);
        } catch (JsonProcessingException e) {
            log.error("[SQS 직렬화 실패]", e);
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
                log.error("[SQS 인증 요청 발행 최종 실패] dto={}", dto);
            }
        }
    }
}
