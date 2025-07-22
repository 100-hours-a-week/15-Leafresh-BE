package ktb.leafresh.backend.domain.store.order.infrastructure.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import jakarta.annotation.PostConstruct;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.store.order.application.dto.PurchaseCommand;
import ktb.leafresh.backend.domain.store.order.domain.entity.PurchaseFailureLog;
import ktb.leafresh.backend.domain.store.order.domain.entity.PurchaseProcessingLog;
import ktb.leafresh.backend.domain.store.order.domain.entity.enums.PurchaseProcessingStatus;
import ktb.leafresh.backend.domain.store.order.infrastructure.repository.PurchaseFailureLogRepository;
import ktb.leafresh.backend.domain.store.order.infrastructure.repository.PurchaseProcessingLogRepository;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("!eks")
@RequiredArgsConstructor
@Slf4j
public class GcpPurchaseDlqMessageSubscriber {

    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final PurchaseFailureLogRepository purchaseFailureLogRepository;
    private final PurchaseProcessingLogRepository purchaseProcessingLogRepository;

    @PostConstruct
    public void subscribe() {
        String projectId = environment.getProperty("gcp.project-id");
        String subscriptionId = environment.getProperty("gcp.pubsub.subscriptions.dlq");

        ProjectSubscriptionName dlqSubscription = ProjectSubscriptionName.of(projectId, subscriptionId);

        MessageReceiver receiver = (message, consumer) -> {
            String rawData = message.getData().toStringUtf8();
            log.error("[DLQ 수신] messageId={}, data={}", message.getMessageId(), rawData);

            try {
                PurchaseCommand failedCommand = objectMapper.readValue(rawData, PurchaseCommand.class);

                purchaseFailureLogRepository.save(PurchaseFailureLog.builder()
                        .member(Member.builder().id(failedCommand.memberId()).build())
                        .product(Product.builder().id(failedCommand.productId()).build())
                        .reason("DLQ로 이동된 메시지입니다.")
                        .requestBody(rawData)
                        .occurredAt(LocalDateTime.now())
                        .build());

                purchaseProcessingLogRepository.save(PurchaseProcessingLog.builder()
                        .product(Product.builder().id(failedCommand.productId()).build())
                        .status(PurchaseProcessingStatus.FAILURE)
                        .message("DLQ 처리됨")
                        .build());

                log.warn("[DLQ 처리] memberId={}, productId={}, quantity={}",
                        failedCommand.memberId(), failedCommand.productId(), failedCommand.quantity());

                // TODO: Discord/DB/이메일 저장 로직 분리

                consumer.ack(); // DLQ는 반드시 ack 처리해야 다시 쌓이지 않음
            } catch (Exception e) {
                log.error("[DLQ 메시지 파싱 실패] {}", e.getMessage(), e);
                consumer.ack(); // 파싱 실패해도 ack로 종료 (무한 재시도 방지)
            }
        };

        Subscriber subscriber = Subscriber.newBuilder(dlqSubscription, receiver).build();
        subscriber.startAsync().awaitRunning();
        log.info("[DLQ 메시지 구독 시작]");
    }
}
