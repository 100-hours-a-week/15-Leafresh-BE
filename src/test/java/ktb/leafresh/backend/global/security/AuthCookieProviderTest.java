package ktb.leafresh.backend.global.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthCookieProvider 테스트")
class AuthCookieProviderTest {

    private AuthCookieProvider authCookieProvider;

    @BeforeEach
    void setUp() {
        authCookieProvider = new AuthCookieProvider();
        
        // @Value 필드들을 테스트 값으로 설정
        ReflectionTestUtils.setField(authCookieProvider, "secure", true);
        ReflectionTestUtils.setField(authCookieProvider, "sameSite", "Strict");
        ReflectionTestUtils.setField(authCookieProvider, "domain", "leafresh.com");
        
        // @PostConstruct 메서드 호출
        authCookieProvider.init();
    }

    @Test
    @DisplayName("일반 쿠키 생성 - 모든 속성이 올바르게 설정되어야 한다")
    void createCookie_ShouldCreateCookieWithCorrectAttributes() {
        // given
        String cookieName = "testCookie";
        String cookieValue = "testValue";
        Duration maxAge = Duration.ofMinutes(30);

        // when
        ResponseCookie cookie = authCookieProvider.createCookie(cookieName, cookieValue, maxAge);

        // then
        assertAll(
                () -> assertThat(cookie.getName()).isEqualTo(cookieName),
                () -> assertThat(cookie.getValue()).isEqualTo(cookieValue),
                () -> assertThat(cookie.isHttpOnly()).isTrue(),
                () -> assertThat(cookie.isSecure()).isTrue(),
                () -> assertThat(cookie.getDomain()).isEqualTo("leafresh.com"),
                () -> assertThat(cookie.getPath()).isEqualTo("/"),
                () -> assertThat(cookie.getSameSite()).isEqualTo("Strict"),
                () -> assertThat(cookie.getMaxAge()).isEqualTo(maxAge)
        );
    }

    @Test
    @DisplayName("쿠키 삭제 - 빈 값과 maxAge 0으로 설정되어야 한다")
    void clearCookie_ShouldCreateCookieWithEmptyValueAndZeroMaxAge() {
        // given
        String cookieName = "testCookie";

        // when
        ResponseCookie cookie = authCookieProvider.clearCookie(cookieName);

        // then
        assertAll(
                () -> assertThat(cookie.getName()).isEqualTo(cookieName),
                () -> assertThat(cookie.getValue()).isEmpty(),
                () -> assertThat(cookie.isHttpOnly()).isTrue(),
                () -> assertThat(cookie.isSecure()).isTrue(),
                () -> assertThat(cookie.getDomain()).isEqualTo("leafresh.com"),
                () -> assertThat(cookie.getPath()).isEqualTo("/"),
                () -> assertThat(cookie.getSameSite()).isEqualTo("Strict"),
                () -> assertThat(cookie.getMaxAge().getSeconds()).isZero()
        );
    }

    @Test
    @DisplayName("액세스 토큰 쿠키 생성 - 이름이 accessToken이고 30분 만료시간이어야 한다")
    void createAccessTokenCookie_ShouldCreateCookieWithCorrectNameAndMaxAge() {
        // given
        String token = "access.token.value";

        // when
        ResponseCookie cookie = authCookieProvider.createAccessTokenCookie(token);

        // then
        assertAll(
                () -> assertThat(cookie.getName()).isEqualTo("accessToken"),
                () -> assertThat(cookie.getValue()).isEqualTo(token),
                () -> assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofMinutes(30)),
                () -> assertThat(cookie.isHttpOnly()).isTrue(),
                () -> assertThat(cookie.isSecure()).isTrue(),
                () -> assertThat(cookie.getDomain()).isEqualTo("leafresh.com"),
                () -> assertThat(cookie.getPath()).isEqualTo("/"),
                () -> assertThat(cookie.getSameSite()).isEqualTo("Strict")
        );
    }

    @Test
    @DisplayName("리프레시 토큰 쿠키 생성 - 이름이 refreshToken이고 7일 만료시간이어야 한다")
    void createRefreshTokenCookie_ShouldCreateCookieWithCorrectNameAndMaxAge() {
        // given
        String token = "refresh.token.value";

        // when
        ResponseCookie cookie = authCookieProvider.createRefreshTokenCookie(token);

        // then
        assertAll(
                () -> assertThat(cookie.getName()).isEqualTo("refreshToken"),
                () -> assertThat(cookie.getValue()).isEqualTo(token),
                () -> assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofDays(7)),
                () -> assertThat(cookie.isHttpOnly()).isTrue(),
                () -> assertThat(cookie.isSecure()).isTrue(),
                () -> assertThat(cookie.getDomain()).isEqualTo("leafresh.com"),
                () -> assertThat(cookie.getPath()).isEqualTo("/"),
                () -> assertThat(cookie.getSameSite()).isEqualTo("Strict")
        );
    }

    @Test
    @DisplayName("액세스 토큰 쿠키 삭제 - accessToken 이름으로 삭제 쿠키가 생성되어야 한다")
    void clearAccessTokenCookie_ShouldCreateClearCookieWithAccessTokenName() {
        // when
        ResponseCookie cookie = authCookieProvider.clearAccessTokenCookie();

        // then
        assertAll(
                () -> assertThat(cookie.getName()).isEqualTo("accessToken"),
                () -> assertThat(cookie.getValue()).isEmpty(),
                () -> assertThat(cookie.getMaxAge().getSeconds()).isZero(),
                () -> assertThat(cookie.isHttpOnly()).isTrue(),
                () -> assertThat(cookie.isSecure()).isTrue(),
                () -> assertThat(cookie.getDomain()).isEqualTo("leafresh.com"),
                () -> assertThat(cookie.getPath()).isEqualTo("/"),
                () -> assertThat(cookie.getSameSite()).isEqualTo("Strict")
        );
    }

    @Test
    @DisplayName("리프레시 토큰 쿠키 삭제 - refreshToken 이름으로 삭제 쿠키가 생성되어야 한다")
    void clearRefreshTokenCookie_ShouldCreateClearCookieWithRefreshTokenName() {
        // when
        ResponseCookie cookie = authCookieProvider.clearRefreshTokenCookie();

        // then
        assertAll(
                () -> assertThat(cookie.getName()).isEqualTo("refreshToken"),
                () -> assertThat(cookie.getValue()).isEmpty(),
                () -> assertThat(cookie.getMaxAge().getSeconds()).isZero(),
                () -> assertThat(cookie.isHttpOnly()).isTrue(),
                () -> assertThat(cookie.isSecure()).isTrue(),
                () -> assertThat(cookie.getDomain()).isEqualTo("leafresh.com"),
                () -> assertThat(cookie.getPath()).isEqualTo("/"),
                () -> assertThat(cookie.getSameSite()).isEqualTo("Strict")
        );
    }

    @Test
    @DisplayName("secure가 false인 환경에서 쿠키 생성 - secure 속성이 false여야 한다")
    void createCookie_WithSecureFalse_ShouldCreateNonSecureCookie() {
        // given
        ReflectionTestUtils.setField(authCookieProvider, "secure", false);
        String cookieName = "testCookie";
        String cookieValue = "testValue";
        Duration maxAge = Duration.ofMinutes(30);

        // when
        ResponseCookie cookie = authCookieProvider.createCookie(cookieName, cookieValue, maxAge);

        // then
        assertThat(cookie.isSecure()).isFalse();
    }

    @Test
    @DisplayName("다른 SameSite 정책으로 쿠키 생성 - SameSite 속성이 올바르게 설정되어야 한다")
    void createCookie_WithDifferentSameSite_ShouldCreateCookieWithCorrectSameSite() {
        // given
        ReflectionTestUtils.setField(authCookieProvider, "sameSite", "Lax");
        String cookieName = "testCookie";
        String cookieValue = "testValue";
        Duration maxAge = Duration.ofMinutes(30);

        // when
        ResponseCookie cookie = authCookieProvider.createCookie(cookieName, cookieValue, maxAge);

        // then
        assertThat(cookie.getSameSite()).isEqualTo("Lax");
    }

    @Test
    @DisplayName("다른 도메인으로 쿠키 생성 - 도메인 속성이 올바르게 설정되어야 한다")
    void createCookie_WithDifferentDomain_ShouldCreateCookieWithCorrectDomain() {
        // given
        ReflectionTestUtils.setField(authCookieProvider, "domain", "example.com");
        String cookieName = "testCookie";
        String cookieValue = "testValue";
        Duration maxAge = Duration.ofMinutes(30);

        // when
        ResponseCookie cookie = authCookieProvider.createCookie(cookieName, cookieValue, maxAge);

        // then
        assertThat(cookie.getDomain()).isEqualTo("example.com");
    }

    @Test
    @DisplayName("null 값으로 쿠키 생성 - null 값이 빈 문자열로 변환되어야 한다")
    void createCookie_WithNullValue_ShouldCreateCookieWithEmptyString() {
        // given
        String cookieName = "testCookie";
        String cookieValue = null;
        Duration maxAge = Duration.ofMinutes(30);

        // when
        ResponseCookie cookie = authCookieProvider.createCookie(cookieName, cookieValue, maxAge);

        // then
        assertThat(cookie.getValue()).isEmpty();
    }

    @Test
    @DisplayName("0 Duration으로 쿠키 생성 - maxAge가 0으로 설정되어야 한다")
    void createCookie_WithZeroDuration_ShouldCreateCookieWithZeroMaxAge() {
        // given
        String cookieName = "testCookie";
        String cookieValue = "testValue";
        Duration maxAge = Duration.ZERO;

        // when
        ResponseCookie cookie = authCookieProvider.createCookie(cookieName, cookieValue, maxAge);

        // then
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ZERO);
    }
}
