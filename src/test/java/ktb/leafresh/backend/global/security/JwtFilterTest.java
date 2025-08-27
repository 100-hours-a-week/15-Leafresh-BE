package ktb.leafresh.backend.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtFilter 테스트")
class JwtFilterTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private AuthCookieProvider authCookieProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 토큰으로 인증 성공")
    void doFilterInternal_ValidToken_AuthenticationSet() throws ServletException, IOException {
        // given
        String validToken = "valid-token";
        Cookie[] cookies = {new Cookie("accessToken", validToken)};

        given(request.getCookies()).willReturn(cookies);
        given(tokenBlacklistService.isBlacklisted(validToken)).willReturn(false);
        given(tokenProvider.validateToken(validToken)).willReturn(true);
        given(tokenProvider.getAuthentication(validToken)).willReturn(authentication);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
    }

    @Test
    @DisplayName("쿠키가 없을 때 인증 없이 필터 통과")
    void doFilterInternal_NoCookies_NoAuthentication() throws ServletException, IOException {
        // given
        given(request.getCookies()).willReturn(null);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("accessToken 쿠키가 없을 때 인증 없이 필터 통과")
    void doFilterInternal_NoAccessTokenCookie_NoAuthentication() throws ServletException, IOException {
        // given
        Cookie[] cookies = {new Cookie("otherCookie", "value")};
        given(request.getCookies()).willReturn(cookies);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("블랙리스트된 토큰으로 인증 없이 필터 통과")
    void doFilterInternal_BlacklistedToken_NoAuthentication() throws ServletException, IOException {
        // given
        String blacklistedToken = "blacklisted-token";
        Cookie[] cookies = {new Cookie("accessToken", blacklistedToken)};

        given(request.getCookies()).willReturn(cookies);
        given(tokenBlacklistService.isBlacklisted(blacklistedToken)).willReturn(true);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider, never()).validateToken(anyString());
        verify(tokenProvider, never()).getAuthentication(anyString());
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 인증 없이 필터 통과")
    void doFilterInternal_InvalidToken_NoAuthentication() throws ServletException, IOException {
        // given
        String invalidToken = "invalid-token";
        Cookie[] cookies = {new Cookie("accessToken", invalidToken)};

        given(request.getCookies()).willReturn(cookies);
        given(tokenBlacklistService.isBlacklisted(invalidToken)).willReturn(false);
        given(tokenProvider.validateToken(invalidToken)).willReturn(false);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider, never()).getAuthentication(anyString());
    }

    @Test
    @DisplayName("토큰 처리 중 CustomException 발생 시 쿠키 삭제")
    void doFilterInternal_CustomException_ClearCookie() throws ServletException, IOException {
        // given
        String validToken = "valid-token";
        Cookie[] cookies = {new Cookie("accessToken", validToken)};
        ResponseCookie clearCookie = ResponseCookie.from("accessToken", "").maxAge(0).build();

        given(request.getCookies()).willReturn(cookies);
        given(tokenBlacklistService.isBlacklisted(validToken)).willReturn(false);
        given(tokenProvider.validateToken(validToken)).willReturn(true);
        given(tokenProvider.getAuthentication(validToken))
                .willThrow(new CustomException(MemberErrorCode.MEMBER_NOT_FOUND, "존재하지 않는 회원입니다."));
        given(authCookieProvider.clearAccessTokenCookie()).willReturn(clearCookie);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(response).addHeader("Set-Cookie", clearCookie.toString());
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("빈 토큰 값으로 인증 없이 필터 통과")
    void doFilterInternal_EmptyToken_NoAuthentication() throws ServletException, IOException {
        // given
        Cookie[] cookies = {new Cookie("accessToken", "")};
        given(request.getCookies()).willReturn(cookies);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("공백 토큰 값으로 인증 없이 필터 통과")
    void doFilterInternal_WhitespaceToken_NoAuthentication() throws ServletException, IOException {
        // given
        Cookie[] cookies = {new Cookie("accessToken", "   ")};
        given(request.getCookies()).willReturn(cookies);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("여러 쿠키 중 accessToken 쿠키 찾기")
    void doFilterInternal_Multiplecookies_FindAccessToken() throws ServletException, IOException {
        // given
        String validToken = "valid-token";
        Cookie[] cookies = {
                new Cookie("sessionId", "session-123"),
                new Cookie("accessToken", validToken),
                new Cookie("otherCookie", "other-value")
        };

        given(request.getCookies()).willReturn(cookies);
        given(tokenBlacklistService.isBlacklisted(validToken)).willReturn(false);
        given(tokenProvider.validateToken(validToken)).willReturn(true);
        given(tokenProvider.getAuthentication(validToken)).willReturn(authentication);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
    }

    @Test
    @DisplayName("토큰 처리 중 RuntimeException 발생 시 인증 없이 통과")
    void doFilterInternal_RuntimeException_NoAuthentication() throws ServletException, IOException {
        // given
        String validToken = "valid-token";
        Cookie[] cookies = {new Cookie("accessToken", validToken)};

        given(request.getCookies()).willReturn(cookies);
        given(tokenBlacklistService.isBlacklisted(validToken)).willReturn(false);
        given(tokenProvider.validateToken(validToken)).willReturn(true);
        given(tokenProvider.getAuthentication(validToken)).willThrow(new RuntimeException("Unexpected error"));

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(response, never()).addHeader(anyString(), anyString());
    }
}
