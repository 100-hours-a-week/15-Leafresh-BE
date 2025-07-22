package ktb.leafresh.backend.domain.store.order.infrastructure.publisher;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ktb.leafresh.backend.domain.store.order.application.dto.PurchaseCommand;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.PurchaseErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("eks")
@Slf4j
@RequiredArgsConstructor
public class AwsPurchaseMessagePublisher implements PurchaseMessagePublisher {

    private final AmazonSQSAsync sqs;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.order-request-queue-url}")
    private String queueUrl;

    @Override
    public void publish(PurchaseCommand command) {
        try {
            String message = objectMapper.writeValueAsString(command);
            sqs.sendMessage(queueUrl, message);
            log.info("[SQS 메시지 발행 성공] {}", message);
        } catch (JsonProcessingException e) {
            log.error("[SQS 직렬화 실패]", e);
            throw new CustomException(PurchaseErrorCode.PURCHASE_SERIALIZATION_FAILED);
        } catch (Exception e) {
            log.error("[SQS 발행 실패]", e);
            throw new CustomException(PurchaseErrorCode.PURCHASE_PUBLISH_FAILED);
        }
    }
}
