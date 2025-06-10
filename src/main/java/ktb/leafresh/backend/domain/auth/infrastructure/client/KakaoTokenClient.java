package ktb.leafresh.backend.domain.auth.infrastructure.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoTokenClient {

    private final WebClient webClient;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.client-secret:}")
    private String clientSecret;

    private static final String TOKEN_REQUEST_URL = "https://kauth.kakao.com/oauth/token";

    @PostConstruct
    public void logConfig() {
        log.info("카카오 TokenClient 설정 로드 완료 - clientId={}, redirectUri={}, clientSecret={}",
                clientId, redirectUri, clientSecret);
    }

    public String getAccessToken(String authorizationCode, String redirectUri) {
        var formData = BodyInserters
                .fromFormData("grant_type", "authorization_code")
                .with("client_id", clientId)
                .with("redirect_uri", redirectUri)
                .with("code", authorizationCode);

        if (clientSecret != null && !clientSecret.isBlank()) {
            formData = formData.with("client_secret", clientSecret);
        }

        log.debug("AccessToken 요청 준비 - code={}, clientId={}, redirectUri={}", authorizationCode, clientId, redirectUri);

        return webClient.post()
                .uri(TOKEN_REQUEST_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        clientResponse -> clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("AccessToken 요청 실패 - errorBody={}", errorBody);
                            return Mono.error(new IllegalStateException("카카오 AccessToken 요청 실패: " + errorBody));
                        })
                )
                .bodyToMono(TokenResponse.class)
                .map(TokenResponse::getAccessToken)
                .blockOptional()
                .orElseThrow(() -> new IllegalStateException("카카오 AccessToken 요청 실패"));
    }

    public record TokenResponse(String access_token) {
        public String getAccessToken() {
            return access_token;
        }
    }
}
