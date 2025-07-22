package ktb.leafresh.backend.domain.store.order.infrastructure.subscriber;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Profile("eks")
@RequiredArgsConstructor
@Slf4j
public class AwsPurchaseDlqMessageSubscriber {

    private final AmazonSQSAsync sqs;
    private final ObjectMapper objectMapper;
    private final PurchaseFailureLogRepository failureLogRepo;
    private final PurchaseProcessingLogRepository processingLogRepo;

    @Value("${aws.sqs.order-dlq-queue-url}")
    private String dlqUrl;

    private static final int WAIT_TIME = 20;
    private static final int MAX_MESSAGES = 5;

    @PostConstruct
    public void startPolling() {
        Executors.newSingleThreadScheduledExecutor()
                .scheduleWithFixedDelay(this::pollDlqMessages, 0, 5, TimeUnit.SECONDS);
        log.info("[SQS 주문 DLQ Subscriber 시작] dlqUrl={}", dlqUrl);
    }

    private void pollDlqMessages() {
        try {
            List<com.amazonaws.services.sqs.model.Message> messages = sqs.receiveMessage(
                    new ReceiveMessageRequest(dlqUrl)
                            .withMaxNumberOfMessages(MAX_MESSAGES)
                            .withWaitTimeSeconds(WAIT_TIME)
            ).getMessages();

            for (var message : messages) {
                String body = message.getBody();
                log.error("[SQS 주문 DLQ 수신] messageId={}, body={}", message.getMessageId(), body);

                try {
                    PurchaseCommand cmd = objectMapper.readValue(body, PurchaseCommand.class);

                    failureLogRepo.save(PurchaseFailureLog.builder()
                            .member(Member.builder().id(cmd.memberId()).build())
                            .product(Product.builder().id(cmd.productId()).build())
                            .reason("DLQ로 이동된 메시지입니다.")
                            .requestBody(body)
                            .occurredAt(LocalDateTime.now())
                            .build());

                    processingLogRepo.save(PurchaseProcessingLog.builder()
                            .product(Product.builder().id(cmd.productId()).build())
                            .status(PurchaseProcessingStatus.FAILURE)
                            .message("DLQ 처리됨")
                            .build());

                } catch (Exception e) {
                    log.error("[DLQ 메시지 파싱 실패] {}", e.getMessage(), e);
                } finally {
                    sqs.deleteMessage(new DeleteMessageRequest(dlqUrl, message.getReceiptHandle()));
                }
            }

        } catch (Exception e) {
            log.error("[SQS 주문 DLQ Polling 실패] {}", e.getMessage(), e);
        }
    }
}
