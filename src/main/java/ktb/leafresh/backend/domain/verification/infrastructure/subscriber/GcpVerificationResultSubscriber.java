package ktb.leafresh.backend.domain.verification.infrastructure.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import jakarta.annotation.PostConstruct;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.verification.application.service.VerificationResultProcessor;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.dto.request.AiVerificationRequestDto;
import ktb.leafresh.backend.domain.verification.infrastructure.publisher.GcpAiVerificationPubSubPublisher;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.VerificationResultRequestDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!eks")
public class GcpVerificationResultSubscriber {

  private final Environment environment;
  private final ObjectMapper objectMapper;
  private final VerificationResultProcessor verificationResultProcessor;
  private final GroupChallengeVerificationRepository groupChallengeVerificationRepository;
  private final GcpAiVerificationPubSubPublisher pubSubPublisher;

  @PostConstruct
  public void subscribe() {
    String projectId = environment.getProperty("gcp.project-id");
    String subscriptionId =
        environment.getProperty("gcp.pubsub.subscriptions.image-verification-result");

    ProjectSubscriptionName subscriptionName =
        ProjectSubscriptionName.of(projectId, subscriptionId);

    MessageReceiver receiver =
        (message, consumer) -> {
          String rawData = message.getData().toStringUtf8();
          log.info("[인증 결과 메시지 수신] messageId={}, data={}", message.getMessageId(), rawData);

          try {
            VerificationResultRequestDto dto =
                objectMapper.readValue(rawData, VerificationResultRequestDto.class);

            if (dto.isSuccessResult()) {
              verificationResultProcessor.process(dto.verificationId(), dto);

            } else if (dto.isRecoverableHttpError()) {
              log.warn(
                  "[AI 처리 오류 응답] verificationId={}, httpStatus={}",
                  dto.verificationId(),
                  dto.result());

              GroupChallengeVerification verification =
                  groupChallengeVerificationRepository
                      .findById(dto.verificationId())
                      .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 인증 ID"));

              GroupChallenge challenge = verification.getParticipantRecord().getGroupChallenge();

              AiVerificationRequestDto retryDto =
                  AiVerificationRequestDto.builder()
                      .verificationId(dto.verificationId())
                      .type(dto.type())
                      .imageUrl(verification.getImageUrl())
                      .memberId(dto.memberId())
                      .challengeId(dto.challengeId())
                      .date(dto.date())
                      .challengeName(challenge.getTitle())
                      .challengeInfo(challenge.getDescription())
                      .build();

              // 비동기 방식으로 재발행
              pubSubPublisher.publishAsyncWithRetry(retryDto);
              log.info("[AI 인증 재발행 요청 전송] verificationId={}", dto.verificationId());

            } else {
              log.error("[알 수 없는 result 값] result={}", dto.result());
            }

            consumer.ack();

          } catch (CustomException e) {
            if (e.getErrorCode() == VerificationErrorCode.VERIFICATION_NOT_FOUND) {
              log.warn("[인증 결과 무시] 존재하지 않는 verificationId={} → ack 처리", e.getMessage());
              consumer.ack(); // 무시하고 ack 처리 (테스트 용도로 AI 서버에서 id가 보낼 수 있기 때문)
            } else {
              log.error("[인증 결과 메시지 처리 실패 - CustomException] {}", e.getMessage(), e);
              consumer.nack(); // 다른 에러는 nack
            }
          } catch (Exception e) {
            log.error("[인증 결과 메시지 처리 실패 - 기타 Exception] {}", e.getMessage(), e);
            consumer.nack();
          }
        };

    Subscriber subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
    subscriber.startAsync().awaitRunning();
    log.info("[인증 결과 메시지 구독 시작]");
  }
}
