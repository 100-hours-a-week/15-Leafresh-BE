package ktb.leafresh.backend.domain.verification.infrastructure.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import jakarta.annotation.PostConstruct;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.verification.domain.entity.VerificationFailureLog;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.VerificationFailureLogRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.VerificationResultRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class GcpVerificationResultDlqMessageSubscriber {

    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final VerificationFailureLogRepository failureLogRepository;
    private final MemberRepository memberRepository;

    @PostConstruct
    public void subscribe() {
        String projectId = environment.getProperty("gcp.project-id");
        String subscriptionId = environment.getProperty("gcp.pubsub.subscriptions.verification-dlq");

        ProjectSubscriptionName dlqSubscription = ProjectSubscriptionName.of(projectId, subscriptionId);

        MessageReceiver receiver = (message, consumer) -> {
            String rawData = message.getData().toStringUtf8();
            log.error("[인증 DLQ 수신] messageId={}, data={}", message.getMessageId(), rawData);

            try {
                VerificationResultRequestDto dto = objectMapper.readValue(rawData, VerificationResultRequestDto.class);

                Member member = memberRepository.findById(dto.memberId())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

                failureLogRepository.save(VerificationFailureLog.builder()
                        .member(member)
                        .challengeType(dto.type())
                        .challengeId(dto.challengeId())
                        .verificationId(dto.verificationId())
                        .reason("DLQ로 이동된 인증 메시지입니다.")
                        .requestBody(rawData)
                        .occurredAt(LocalDateTime.now())
                        .build());
            } catch (Exception e) {
                log.warn("[DLQ 메시지 파싱 실패 → 최소 정보로 로그 저장] {}", e.getMessage(), e);

                failureLogRepository.save(VerificationFailureLog.builder()
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
        log.info("[인증 DLQ 메시지 구독 시작]");
    }
}
