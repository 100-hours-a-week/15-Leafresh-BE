package ktb.leafresh.backend.domain.store.order.infrastructure.subscriber;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import ktb.leafresh.backend.domain.store.order.application.dto.PurchaseCommand;
import ktb.leafresh.backend.domain.store.order.application.service.ProductPurchaseProcessingService;
import ktb.leafresh.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Profile("eks")
@RequiredArgsConstructor
@Slf4j
public class AwsPurchaseMessageSubscriber {

  private final AmazonSQSAsync sqs;
  private final ObjectMapper objectMapper;
  private final ProductPurchaseProcessingService processingService;

  @Value("${aws.sqs.order-request-queue-url}")
  private String queueUrl;

  private static final int WAIT_TIME = 20;
  private static final int MAX_MESSAGES = 5;

  @PostConstruct
  public void startPolling() {
    Executors.newSingleThreadScheduledExecutor()
        .scheduleWithFixedDelay(this::pollMessages, 0, 5, TimeUnit.SECONDS);
    log.info("[SQS 주문 Subscriber 시작] queueUrl={}", queueUrl);
  }

  private void pollMessages() {
    try {
      ReceiveMessageRequest req =
          new ReceiveMessageRequest(queueUrl)
              .withMaxNumberOfMessages(MAX_MESSAGES)
              .withWaitTimeSeconds(WAIT_TIME);

      List<com.amazonaws.services.sqs.model.Message> messages =
          sqs.receiveMessage(req).getMessages();

      for (var message : messages) {
        String body = message.getBody();
        log.info("[SQS 주문 수신] messageId={}, body={}", message.getMessageId(), body);

        try {
          PurchaseCommand cmd = objectMapper.readValue(body, PurchaseCommand.class);
          processingService.process(cmd);
          sqs.deleteMessage(new DeleteMessageRequest(queueUrl, message.getReceiptHandle()));
        } catch (CustomException e) {
          log.error("[처리 실패 - CustomException] {}", e.getMessage(), e);
          // Retry: 메시지 삭제 없이 재시도
        } catch (Exception e) {
          log.error("[처리 실패 - Exception] {}", e.getMessage(), e);
        }
      }

    } catch (Exception e) {
      log.error("[SQS Polling 실패] {}", e.getMessage(), e);
    }
  }
}
