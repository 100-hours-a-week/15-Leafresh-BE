package ktb.leafresh.backend.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ktb.leafresh.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  private final TokenProvider tokenProvider;
  private final TokenBlacklistService tokenBlacklistService;
  private final AuthCookieProvider authCookieProvider;

  // 실제 필터링 로직은 doFilterInternal 에 들어감
  // JWT 토큰의 인증 정보를 현재 쓰레드의 SecurityContext 에 저장하는 역할 수행
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {

    // 1. cookie 에서 토큰을 꺼냄
    String jwt = resolveToken(request);
    log.debug("JWT from cookie: {}", jwt);

    try {
      // 1. accessToken이 있고 블랙리스트에 포함되어 있으면 → 인증 없이 필터만 통과 (로그아웃된 토큰)
      if (StringUtils.hasText(jwt) && tokenBlacklistService.isBlacklisted(jwt)) {
        log.warn("블랙리스트 토큰입니다. 필터 통과만 시킴");
        filterChain.doFilter(request, response);
        return;
      }

      // 2. accessToken이 있고 유효하면 → 사용자 인증 객체 생성하여 SecurityContext에 저장
      if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
        Authentication authentication = tokenProvider.getAuthentication(jwt);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }

    } catch (CustomException ex) {
      // 3. 토큰 처리 도중 오류가 발생한 경우 (ex: DB에 사용자 없음) → 쿠키 제거
      log.warn("토큰 인증 중 예외 발생: {} → accessToken 쿠키 삭제", ex.getMessage());

      // accessToken 쿠키 제거 (브라우저에서 자동 삭제됨)
      response.addHeader("Set-Cookie", authCookieProvider.clearAccessTokenCookie().toString());
    } catch (Exception ex) {
      // 4. 기타 예외 발생 시 로그만 남기고 인증 없이 통과
      log.warn("토큰 처리 중 예상치 못한 예외 발생: {}", ex.getMessage());
    }

    filterChain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest request) {
    // 쿠키에서 accessToken 추출
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("accessToken".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }

    return null;
  }
}
