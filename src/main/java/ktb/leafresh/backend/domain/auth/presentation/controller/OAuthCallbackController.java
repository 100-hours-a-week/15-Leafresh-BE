package ktb.leafresh.backend.domain.auth.presentation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ktb.leafresh.backend.domain.auth.application.service.oauth.OAuthLoginService;
import ktb.leafresh.backend.domain.auth.presentation.dto.response.OAuthLoginResponseDto;
import ktb.leafresh.backend.domain.auth.presentation.dto.response.OAuthTokenResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.AuthCookieProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OAuthCallbackController {

  private final OAuthLoginService oAuthLoginService;
  private final AuthCookieProvider authCookieProvider;

  @GetMapping("/member/kakao/callback")
  public ResponseEntity<ApiResponse<OAuthLoginResponseDto>> kakaoCallback(
      @RequestParam String code, HttpServletRequest request, HttpServletResponse response) {
    log.info("인가 코드 수신 - code={}", code);

    // 실제 요청 URL 전체를 redirect_uri로 사용 (카카오 등록값과 반드시 일치해야 함)
    String redirectUri = request.getRequestURL().toString();
    log.info("실제 redirectUri: {}", redirectUri);

    OAuthTokenResponseDto tokenDto = oAuthLoginService.loginWithKakao(code, redirectUri);

    log.info(
        "카카오 로그인 토큰 발급 완료 - accessToken={}, refreshToken={}",
        tokenDto.accessToken(),
        tokenDto.refreshToken());

    OAuthLoginResponseDto loginData =
        new OAuthLoginResponseDto(
            tokenDto.accessToken() != null,
            tokenDto.email(),
            tokenDto.nickname(),
            tokenDto.imageUrl());

    if (tokenDto.accessToken() == null || tokenDto.accessTokenExpiresIn() == null) {
      response.addHeader(
          HttpHeaders.SET_COOKIE, authCookieProvider.clearAccessTokenCookie().toString());
      response.addHeader(
          HttpHeaders.SET_COOKIE, authCookieProvider.clearRefreshTokenCookie().toString());

      return ResponseEntity.ok(ApiResponse.success("첫 회원가입 사용자 카카오 로그인 성공 (추가 정보 필요)", loginData));
    }

    response.addHeader(
        HttpHeaders.SET_COOKIE,
        authCookieProvider.createAccessTokenCookie(tokenDto.accessToken()).toString());
    response.addHeader(
        HttpHeaders.SET_COOKIE,
        authCookieProvider.createRefreshTokenCookie(tokenDto.refreshToken()).toString());

    return ResponseEntity.ok(ApiResponse.success("카카오 로그인에 성공하였습니다.", loginData));
  }
}
