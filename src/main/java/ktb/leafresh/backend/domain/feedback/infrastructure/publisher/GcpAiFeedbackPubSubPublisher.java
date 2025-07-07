package ktb.leafresh.backend.domain.feedback.infrastructure.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import ktb.leafresh.backend.domain.feedback.domain.entity.FeedbackFailureLog;
import ktb.leafresh.backend.domain.feedback.infrastructure.dto.request.AiFeedbackCreationRequestDto;
import ktb.leafresh.backend.domain.feedback.infrastructure.repository.FeedbackFailureLogRepository;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class GcpAiFeedbackPubSubPublisher {

    private final Publisher feedbackPublisher;
    private final ObjectMapper objectMapper;
    private final MemberRepository memberRepository;
    private final FeedbackFailureLogRepository feedbackFailureLogRepository;

    private static final int MAX_RETRY = 3;
    private static final long INITIAL_BACKOFF_MS = 300;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public GcpAiFeedbackPubSubPublisher(
            @Qualifier("feedbackPubSubPublisher") Publisher feedbackPublisher,
            ObjectMapper objectMapper,
            MemberRepository memberRepository,
            FeedbackFailureLogRepository feedbackFailureLogRepository
    ) {
        this.feedbackPublisher = feedbackPublisher;
        this.objectMapper = objectMapper;
        this.memberRepository = memberRepository;
        this.feedbackFailureLogRepository = feedbackFailureLogRepository;
    }

    public void publishAsyncWithRetry(AiFeedbackCreationRequestDto dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            sendWithRetry(json, 1);
        } catch (JsonProcessingException e) {
            log.error("[피드백 직렬화 실패]", e);
            logFailure(dto, null, "직렬화 실패: " + e.getMessage());
        }
    }

    private void sendWithRetry(String json, int attempt) {
        PubsubMessage message = PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(json))
                .build();

        ApiFuture<String> future = feedbackPublisher.publish(message);

        ApiFutures.addCallback(future, new ApiFutureCallback<>() {
            @Override
            public void onSuccess(String messageId) {
                log.info("[피드백 발행 성공] attempt={}, messageId={}", attempt, messageId);
            }

            @Override
            public void onFailure(Throwable t) {
                log.warn("[피드백 발행 실패] attempt={}, error={}", attempt, t.getMessage());

                if (attempt < MAX_RETRY) {
                    long backoff = INITIAL_BACKOFF_MS * (1L << (attempt - 1));
                    scheduler.schedule(() -> sendWithRetry(json, attempt + 1), backoff, TimeUnit.MILLISECONDS);
                } else {
                    log.error("[피드백 발행 최종 실패] json={}, error={}", json, t.getMessage());
                }
            }
        }, MoreExecutors.directExecutor());
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
