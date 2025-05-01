package ktb.leafresh.backend.global.security;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieProvider {

    private static final String ACCESS_TOKEN_NAME = "accessToken";
    private static final String REFRESH_TOKEN_NAME = "refreshToken";

    public ResponseCookie createAccessTokenCookie(String accessToken, long expiresInMillis) {
        return ResponseCookie.from(ACCESS_TOKEN_NAME, accessToken)
                .httpOnly(true).secure(true).path("/").sameSite("Strict")
                .maxAge(Duration.ofMillis(expiresInMillis)).build();
    }

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_NAME, refreshToken)
                .httpOnly(true).secure(true).path("/").sameSite("Strict")
                .maxAge(Duration.ofDays(7)).build(); // 7일 고정
    }

    public ResponseCookie clearAccessTokenCookie() {
        return ResponseCookie.from(ACCESS_TOKEN_NAME, "")
                .httpOnly(true).secure(true).path("/").sameSite("Strict")
                .maxAge(0).build();
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_NAME, "")
                .httpOnly(true).secure(true).path("/").sameSite("Strict")
                .maxAge(0).build();
    }
}
