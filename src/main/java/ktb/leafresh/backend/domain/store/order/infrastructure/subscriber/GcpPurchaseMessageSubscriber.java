package ktb.leafresh.backend.domain.store.order.infrastructure.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.cloud.pubsub.v1.MessageReceiver;
import ktb.leafresh.backend.domain.store.order.application.dto.PurchaseCommand;
import ktb.leafresh.backend.domain.store.order.application.service.ProductPurchaseProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Slf4j
@Profile("!eks")
@Component
@RequiredArgsConstructor
public class GcpPurchaseMessageSubscriber {

    private final Environment environment;
    private final ProductPurchaseProcessingService processingService;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void subscribe() {
        String projectId = environment.getProperty("gcp.project-id");
        String subscriptionId = environment.getProperty("gcp.pubsub.subscriptions.order");

        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);

        MessageReceiver receiver = (message, consumer) -> {
            String rawData = message.getData().toStringUtf8();
            String attemptStr = message.getAttributesOrDefault("googclient_deliveryattempt", null);
            Integer attempt = attemptStr != null ? Integer.parseInt(attemptStr) : null;

            log.info("[메시지 수신] messageId={}, deliveryAttempt={}, data={}",
                    message.getMessageId(), attempt, rawData);

            // DLQ 대상이면 더 이상 처리하지 않고 ack() 후 종료
            if (attempt != null && attempt >= 5) {
                log.warn("[deliveryAttempt {}] DLQ 대상 - nack 처리하여 DLQ로 이동 시도", attempt);
                consumer.ack(); // GCP가 DLQ로 이동시킴
                return;
            }

            try {
                PurchaseCommand command = objectMapper.readValue(rawData, PurchaseCommand.class);
                processingService.process(command);
                consumer.ack(); // 성공적으로 처리되면 ack
            } catch (Exception e) {
                log.error("[구매 메시지 처리 실패] {}", e.getMessage(), e);
                consumer.nack(); // 실패 시 재시도 (DLQ 조건에 따라)
            }
        };

        Subscriber subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
        subscriber.startAsync().awaitRunning();
        log.info("[구매 메시지 구독 시작]");
    }
}
