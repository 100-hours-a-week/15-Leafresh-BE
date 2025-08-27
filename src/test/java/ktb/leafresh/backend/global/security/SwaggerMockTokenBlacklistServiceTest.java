package ktb.leafresh.backend.global.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SwaggerMockTokenBlacklistService 테스트")
class SwaggerMockTokenBlacklistServiceTest {

    private final SwaggerMockTokenBlacklistService mockService = new SwaggerMockTokenBlacklistService();

    @Test
    @DisplayName("액세스 토큰 블랙리스트 등록 - Mock 동작")
    void blacklistAccessToken_MockBehavior() {
        // given
        String accessToken = "test-access-token";
        long expirationTimeMillis = 30 * 60 * 1000; // 30분

        // when & then - 예외 발생하지 않음
        mockService.blacklistAccessToken(accessToken, expirationTimeMillis);
    }

    @Test
    @DisplayName("토큰 블랙리스트 확인 - 항상 false 반환")
    void isBlacklisted_AlwaysReturnsFalse() {
        // given
        String accessToken = "test-access-token";

        // when
        boolean result = mockService.isBlacklisted(accessToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("null 토큰 블랙리스트 등록 - Mock 동작")
    void blacklistAccessToken_WithNullToken_MockBehavior() {
        // given
        String accessToken = null;
        long expirationTimeMillis = 30 * 60 * 1000;

        // when & then - 예외 발생하지 않음
        mockService.blacklistAccessToken(accessToken, expirationTimeMillis);
    }

    @Test
    @DisplayName("null 토큰 블랙리스트 확인 - 항상 false 반환")
    void isBlacklisted_WithNullToken_AlwaysReturnsFalse() {
        // given
        String accessToken = null;

        // when
        boolean result = mockService.isBlacklisted(accessToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("빈 문자열 토큰 블랙리스트 등록 - Mock 동작")
    void blacklistAccessToken_WithEmptyToken_MockBehavior() {
        // given
        String accessToken = "";
        long expirationTimeMillis = 30 * 60 * 1000;

        // when & then - 예외 발생하지 않음
        mockService.blacklistAccessToken(accessToken, expirationTimeMillis);
    }

    @Test
    @DisplayName("빈 문자열 토큰 블랙리스트 확인 - 항상 false 반환")
    void isBlacklisted_WithEmptyToken_AlwaysReturnsFalse() {
        // given
        String accessToken = "";

        // when
        boolean result = mockService.isBlacklisted(accessToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("0 만료시간으로 토큰 블랙리스트 등록 - Mock 동작")
    void blacklistAccessToken_WithZeroExpiration_MockBehavior() {
        // given
        String accessToken = "test-token";
        long expirationTimeMillis = 0;

        // when & then - 예외 발생하지 않음
        mockService.blacklistAccessToken(accessToken, expirationTimeMillis);
    }

    @Test
    @DisplayName("음수 만료시간으로 토큰 블랙리스트 등록 - Mock 동작")
    void blacklistAccessToken_WithNegativeExpiration_MockBehavior() {
        // given
        String accessToken = "test-token";
        long expirationTimeMillis = -1000;

        // when & then - 예외 발생하지 않음
        mockService.blacklistAccessToken(accessToken, expirationTimeMillis);
    }

    @Test
    @DisplayName("매우 긴 토큰으로 블랙리스트 등록 - Mock 동작")
    void blacklistAccessToken_WithVeryLongToken_MockBehavior() {
        // given
        String accessToken = "a".repeat(10000); // 10,000 문자의 토큰
        long expirationTimeMillis = 30 * 60 * 1000;

        // when & then - 예외 발생하지 않음
        mockService.blacklistAccessToken(accessToken, expirationTimeMillis);
    }

    @Test
    @DisplayName("매우 긴 토큰 블랙리스트 확인 - 항상 false 반환")
    void isBlacklisted_WithVeryLongToken_AlwaysReturnsFalse() {
        // given
        String accessToken = "a".repeat(10000); // 10,000 문자의 토큰

        // when
        boolean result = mockService.isBlacklisted(accessToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("특수 문자 토큰 블랙리스트 확인 - 항상 false 반환")
    void isBlacklisted_WithSpecialCharacters_AlwaysReturnsFalse() {
        // given
        String accessToken = "!@#$%^&*()_+-=[]{}|;:,.<>?";

        // when
        boolean result = mockService.isBlacklisted(accessToken);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("유니코드 문자 토큰 블랙리스트 확인 - 항상 false 반환")
    void isBlacklisted_WithUnicodeCharacters_AlwaysReturnsFalse() {
        // given
        String accessToken = "토큰테스트한글🎉";

        // when
        boolean result = mockService.isBlacklisted(accessToken);

        // then
        assertThat(result).isFalse();
    }
}
