package ktb.leafresh.backend.global.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieProvider {

    @Value("${cookie.secure}")
    private boolean secure;

    @Value("${cookie.samesite:Strict}") // 기본은 Strict
    private String sameSite;

    public ResponseCookie createCookie(String name, String value, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite(sameSite)
                .maxAge(maxAge);

        return builder.build();
    }

    public ResponseCookie clearCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite(sameSite)
                .maxAge(0)
                .build();
    }

    public ResponseCookie createAccessTokenCookie(String token, long expiresInMillis) {
        return createCookie("accessToken", token, Duration.ofMillis(expiresInMillis));
    }

    public ResponseCookie createRefreshTokenCookie(String token) {
        return createCookie("refreshToken", token, Duration.ofDays(7));
    }

    public ResponseCookie clearAccessTokenCookie() {
        return clearCookie("accessToken");
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return clearCookie("refreshToken");
    }
}
