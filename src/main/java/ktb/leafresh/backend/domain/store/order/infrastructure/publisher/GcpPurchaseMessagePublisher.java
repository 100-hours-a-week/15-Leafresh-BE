package ktb.leafresh.backend.domain.store.order.infrastructure.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import ktb.leafresh.backend.domain.store.order.application.dto.PurchaseCommand;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!local")
@RequiredArgsConstructor
@Slf4j
public class GcpPurchaseMessagePublisher implements PurchaseMessagePublisher {

    @Qualifier("purchasePubSubPublisher")
    private final Publisher publisher;

    private final ObjectMapper objectMapper;

    @Override
    public void publish(PurchaseCommand command) {
        try {
            String message = objectMapper.writeValueAsString(command);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                    .setData(ByteString.copyFromUtf8(message))
                    .build();

            publisher.publish(pubsubMessage); // 비동기 발행
            log.info("[PubSub 메시지 발행 성공] {}", message);
        } catch (JsonProcessingException e) {
            log.error("[PubSub 직렬화 실패]", e);
            throw new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "구매 요청 직렬화에 실패했습니다.");
        } catch (Exception e) {
            log.error("[PubSub 발행 실패]", e);
            throw new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "구매 요청 발행에 실패했습니다.");
        }
    }
}
