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
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.net.SocketTimeoutException;
import java.time.Duration;

@Slf4j
@Component
@Profile("!local") // prod
public class HttpAiVerificationClient implements AiVerificationClient {

    private final WebClient aiServerWebClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
                    .block(Duration.ofSeconds(5));
//                    .block();

            log.info("[AI 응답 수신 완료]");
            log.debug("[AI 응답 원문 JSON] {}", rawJson);

            if (rawJson == null || rawJson.isBlank()) {
                log.error("[AI 응답 에러] 응답이 null 혹은 빈 문자열입니다. verificationId={}", requestDto.verificationId());
                throw new CustomException(VerificationErrorCode.AI_SERVER_ERROR);
            }

            AiVerificationApiResponseDto parsed;
            try {
                parsed = objectMapper.readValue(rawJson, AiVerificationApiResponseDto.class);
            } catch (Exception parseEx) {
                log.error("[AI 응답 파싱 실패] JSON 파싱 중 예외 발생: {}", parseEx.getMessage(), parseEx);
                log.error("[AI 응답 원문(JSON)] {}", rawJson); // 파싱 실패한 원본도 같이 출력
                throw new CustomException(VerificationErrorCode.AI_SERVER_ERROR);
            }

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

            log.info("[검열 통과] AI 응답 result=true. verificationId={}", requestDto.verificationId());

        } catch (CustomException e) {
            throw e;
        } catch (WebClientRequestException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof SocketTimeoutException) {
                log.error("[AI 서버 타임아웃] 응답 대기 중 시간 초과. verificationId={}", requestDto.verificationId(), ex);
                throw new CustomException(VerificationErrorCode.AI_REQUEST_TIMEOUT);
            }
            log.error("[AI 서버 연결 실패] WebClient 요청 중 예외 발생. verificationId={}", requestDto.verificationId(), ex);
            throw new CustomException(VerificationErrorCode.AI_CONNECTION_FAILED);

        } catch (Exception e) {
            log.error("[AI 이미지 검열 중 알 수 없는 예외 발생] {}", e.getMessage(), e);
            throw new CustomException(VerificationErrorCode.AI_SERVER_ERROR);
        }
    }
}
