package ktb.leafresh.backend.domain.feedback.infrastructure.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import jakarta.annotation.PostConstruct;
import ktb.leafresh.backend.domain.feedback.domain.entity.FeedbackFailureLog;
import ktb.leafresh.backend.domain.feedback.infrastructure.repository.FeedbackFailureLogRepository;
import ktb.leafresh.backend.domain.feedback.presentation.dto.request.FeedbackResultRequestDto;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!eks")
public class GcpAiFeedbackResultDlqSubscriber {

  private final Environment environment;
  private final ObjectMapper objectMapper;
  private final FeedbackFailureLogRepository failureLogRepository;
  private final MemberRepository memberRepository;

  @PostConstruct
  public void subscribe() {
    String projectId = environment.getProperty("gcp.project-id");
    String subscriptionId = environment.getProperty("gcp.pubsub.subscriptions.feedback-result-dlq");

    ProjectSubscriptionName dlqSubscription = ProjectSubscriptionName.of(projectId, subscriptionId);

    MessageReceiver receiver =
        (message, consumer) -> {
          String rawData = message.getData().toStringUtf8();
          log.error("[피드백 DLQ 수신] messageId={}, data={}", message.getMessageId(), rawData);

          try {
            FeedbackResultRequestDto dto =
                objectMapper.readValue(rawData, FeedbackResultRequestDto.class);

            Member member =
                memberRepository
                    .findById(dto.memberId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

            failureLogRepository.save(
                FeedbackFailureLog.builder()
                    .member(member)
                    .reason("DLQ로 이동된 피드백 메시지입니다.")
                    .requestBody(rawData)
                    .occurredAt(LocalDateTime.now())
                    .build());
          } catch (Exception e) {
            log.warn("[DLQ 메시지 파싱 실패] 최소 정보로 로그 저장. error={}", e.getMessage());

            failureLogRepository.save(
                FeedbackFailureLog.builder()
                    .reason("DLQ 메시지 파싱 실패: " + e.getMessage())
                    .requestBody(rawData)
                    .occurredAt(LocalDateTime.now())
                    .build());
          } finally {
            consumer.ack(); // DLQ는 무조건 ack (루프 방지)
          }
        };

    Subscriber subscriber = Subscriber.newBuilder(dlqSubscription, receiver).build();
    subscriber.startAsync().awaitRunning();
    log.info("[피드백 DLQ 구독 시작]");
  }
}
