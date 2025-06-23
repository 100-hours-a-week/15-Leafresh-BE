package ktb.leafresh.backend.domain.verification.infrastructure.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import ktb.leafresh.backend.domain.verification.infrastructure.dto.request.AiVerificationRequestDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!local")
@Slf4j
public class GcpAiVerificationPubSubPublisher {

    private final Publisher imageVerificationPublisher;
    private final ObjectMapper objectMapper;

    public GcpAiVerificationPubSubPublisher(
            @Qualifier("imageVerificationPubSubPublisher") Publisher imageVerificationPublisher,
            ObjectMapper objectMapper
    ) {
        this.imageVerificationPublisher = imageVerificationPublisher;
        this.objectMapper = objectMapper;
    }

    public void publish(AiVerificationRequestDto dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            PubsubMessage message = PubsubMessage.newBuilder()
                    .setData(ByteString.copyFromUtf8(json))
                    .build();

            imageVerificationPublisher.publish(message);
            log.info("[AI 인증 요청 Pub/Sub 메시지 발행 성공] {}", json);
        } catch (JsonProcessingException e) {
            log.error("[AI 인증 직렬화 실패]", e);
            throw new CustomException(VerificationErrorCode.VERIFICATION_SERIALIZATION_FAILED);
        } catch (Exception e) {
            log.error("[AI 인증 Pub/Sub 발행 실패]", e);
            throw new CustomException(VerificationErrorCode.VERIFICATION_PUBLISH_FAILED);
        }
    }
}
