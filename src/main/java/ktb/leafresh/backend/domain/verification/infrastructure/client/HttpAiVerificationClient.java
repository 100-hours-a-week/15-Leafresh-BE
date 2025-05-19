package ktb.leafresh.backend.domain.verification.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import ktb.leafresh.backend.domain.verification.infrastructure.dto.request.AiVerificationRequestDto;
import ktb.leafresh.backend.domain.verification.infrastructure.dto.response.AiVerificationApiResponseDto;
import ktb.leafresh.backend.domain.verification.infrastructure.dto.response.AiVerificationResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@Profile("!local")
public class HttpAiVerificationClient implements AiVerificationClient {

    private final WebClient aiServerWebClient;

    public HttpAiVerificationClient(
            @Qualifier("aiServerWebClient") WebClient aiServerWebClient
    ) {
        this.aiServerWebClient = aiServerWebClient;
    }

    @Override
    public void verifyImage(AiVerificationRequestDto requestDto) {
        try {
            log.info("[AI 이미지 검열 요청] 시작");
            log.debug("[AI 요청 DTO] {}", requestDto);

            String rawJson = aiServerWebClient.post()
                    .uri("/ai/image/verification")
                    .bodyValue(requestDto)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("[AI 응답 수신 완료]");
            log.debug("[AI 응답 원문 JSON] {}", rawJson);

            AiVerificationApiResponseDto parsed =
                    new ObjectMapper().readValue(rawJson, AiVerificationApiResponseDto.class);

            if (parsed.status() == 202) {
                log.info("[AI 응답] 인증 요청 정상 접수됨. 콜백을 기다립니다.");
                return;
            }

            AiVerificationResponseDto result = parsed.data();
            if (result == null) {
                log.warn("[AI 응답] data 필드가 null입니다. 콜백 방식이므로 무시합니다. verificationId={}", requestDto.verificationId());
                return;
            }

            log.debug("[AI 응답 파싱 완료] result={}", result.result());

            if (!result.result()) {
                log.warn("[검열 실패] AI 응답 result=false");
                throw new CustomException(VerificationErrorCode.AI_VERIFICATION_FAILED);
            }

            log.info("[검열 통과] AI 응답 result=true");

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[AI 이미지 검열 중 예외 발생] {}", e.getMessage(), e);
            throw new CustomException(VerificationErrorCode.AI_SERVER_ERROR);
        }
    }
}
