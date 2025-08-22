package ktb.leafresh.backend.domain.verification.infrastructure.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.verification.domain.entity.VerificationFailureLog;
import ktb.leafresh.backend.domain.verification.infrastructure.dto.request.AiVerificationRequestDto;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.VerificationFailureLogRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;

import java.util.concurrent.*;

@Component
@Slf4j
@Profile("!eks")
public class GcpAiVerificationPubSubPublisher implements AiVerificationPublisher {

  private final Publisher imageVerificationPublisher;
  private final ObjectMapper objectMapper;
  private final MemberRepository memberRepository;
  private final VerificationFailureLogRepository failureLogRepository;

  private static final int MAX_RETRY = 3;
  private static final long INITIAL_BACKOFF_MS = 300;

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

  public GcpAiVerificationPubSubPublisher(
      @Qualifier("imageVerificationPubSubPublisher") Publisher imageVerificationPublisher,
      ObjectMapper objectMapper,
      MemberRepository memberRepository,
      VerificationFailureLogRepository failureLogRepository) {
    this.imageVerificationPublisher = imageVerificationPublisher;
    this.objectMapper = objectMapper;
    this.memberRepository = memberRepository;
    this.failureLogRepository = failureLogRepository;
  }

  public void publishAsyncWithRetry(AiVerificationRequestDto dto) {
    try {
      String json = objectMapper.writeValueAsString(dto);
      sendWithRetry(json, dto, 1);
    } catch (JsonProcessingException e) {
      log.error("[AI 인증 직렬화 실패]", e);
      logFailure(dto, null, "직렬화 실패: " + e.getMessage());
    }
  }

  private void sendWithRetry(String json, AiVerificationRequestDto dto, int attempt) {
    PubsubMessage message =
        PubsubMessage.newBuilder().setData(ByteString.copyFromUtf8(json)).build();

    ApiFuture<String> future = imageVerificationPublisher.publish(message);

    ApiFutures.addCallback(
        future,
        new ApiFutureCallback<>() {
          @Override
          public void onSuccess(String messageId) {
            log.info("[AI 인증 요청 발행 성공] attempt={}, messageId={}, dto={}", attempt, messageId, json);
          }

          @Override
          public void onFailure(Throwable t) {
            log.warn("[AI 인증 발행 실패] attempt={}, error={}", attempt, t.getMessage());

            if (attempt < MAX_RETRY) {
              long backoff = INITIAL_BACKOFF_MS * (1L << (attempt - 1)); // 지수 백오프

              scheduler.schedule(
                  () -> {
                    sendWithRetry(json, dto, attempt + 1);
                  },
                  backoff,
                  TimeUnit.MILLISECONDS);

            } else {
              log.error("[AI 인증 발행 최종 실패] dto={}, error={}", json, t.getMessage());
              logFailure(dto, json, "최대 재시도 초과: " + t.getMessage());
            }
          }
        },
        MoreExecutors.directExecutor());
  }

  private void logFailure(AiVerificationRequestDto dto, String json, String reason) {
    try {
      Member member =
          memberRepository
              .findById(dto.memberId())
              .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

      failureLogRepository.save(
          VerificationFailureLog.builder()
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
