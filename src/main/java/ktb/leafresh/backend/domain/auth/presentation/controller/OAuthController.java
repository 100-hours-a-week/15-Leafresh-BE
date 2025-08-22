package ktb.leafresh.backend.domain.auth.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import ktb.leafresh.backend.domain.auth.application.service.oauth.OAuthLoginService;
import ktb.leafresh.backend.domain.auth.application.service.oauth.OAuthReissueTokenService;
import ktb.leafresh.backend.domain.auth.presentation.dto.response.OAuthLoginResponseDto;
import ktb.leafresh.backend.domain.auth.presentation.dto.response.OAuthRedirectUrlResponseDto;
import ktb.leafresh.backend.domain.auth.presentation.dto.response.OAuthTokenResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.response.ApiResponseConstants;
import ktb.leafresh.backend.global.security.AuthCookieProvider;
import ktb.leafresh.backend.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "OAuth 인증", description = "OAuth 로그인 및 토큰 관리 API")
@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class OAuthController {

  private final OAuthLoginService oAuthLoginService;
  private final OAuthReissueTokenService oAuthReissueTokenService;
  private final AuthCookieProvider authCookieProvider;
  private final JwtProvider jwtProvider;

  @GetMapping("/success")
  public ResponseEntity<String> oauthSuccessPage() {
    return ResponseEntity.ok("<h1>카카오 로그인 성공</h1><p>쿠키 확인은 개발자 도구에서</p>");
  }

  @Operation(summary = "카카오 로그인 리다이렉트", description = "카카오 인증 페이지로 리다이렉트합니다.")
  @ApiResponseConstants.RedirectResponses
  @GetMapping("/{provider}")
  public ResponseEntity<ApiResponse<OAuthRedirectUrlResponseDto>> redirectToProvider(
      @Parameter(description = "OAuth 제공자", example = "kakao") @PathVariable String provider,
      @Parameter(description = "클라이언트 도메인", example = "https://leafresh.app")
          @RequestParam(required = false)
          String origin) {
    if (origin == null || origin.isBlank()) {
      origin = "https://leafresh.app"; // fallback 도메인
    }

    String state = jwtProvider.generateStateToken(origin);
    String redirectUrl =
        "https://kauth.kakao.com/oauth/authorize"
            + "?client_id="
            + oAuthLoginService.getClientId()
            + "&redirect_uri="
            + origin
            + "/member/"
            + provider
            + "/callback"
            + "&response_type=code"
            + "&state="
            + state;

    return ResponseEntity.ok(
        ApiResponse.success("소셜 로그인 URL을 반환합니다.", new OAuthRedirectUrlResponseDto(redirectUrl)));
  }

  @Operation(summary = "카카오 로그인 콜백", description = "인가 코드를 받아 JWT를 발급하고 쿠키에 저장하며 사용자 정보를 반환합니다.")
  @ApiResponseConstants.SuccessResponses
  @ApiResponseConstants.ClientErrorResponses
  @ApiResponseConstants.ServerErrorResponses
  @GetMapping("/{provider}/callback")
  public ResponseEntity<ApiResponse<OAuthLoginResponseDto>> kakaoCallback(
      @Parameter(description = "OAuth 제공자", example = "kakao") @PathVariable String provider,
      @Parameter(description = "인가 코드", required = true, example = "abc123") @RequestParam
          String code,
      @Parameter(description = "상태 토큰", required = true) @RequestParam String state,
      HttpServletResponse response) {
    log.info("인가 코드 수신 - code={}", code);

    String origin = jwtProvider.parseStateToken(state);
    log.info("복호화된 origin: {}", origin);

    String redirectUri = origin + "/member/kakao/callback";
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

    // 신규 회원은 쿠키 발급 생략
    if (tokenDto.accessToken() == null || tokenDto.accessTokenExpiresIn() == null) {
      response.addHeader(
          HttpHeaders.SET_COOKIE, authCookieProvider.clearAccessTokenCookie().toString());
      response.addHeader(
          HttpHeaders.SET_COOKIE, authCookieProvider.clearRefreshTokenCookie().toString());

      return ResponseEntity.ok(ApiResponse.success("첫 회원가입 사용자 카카오 로그인 성공 (추가 정보 필요)", loginData));
    }

    addLoginCookies(response, tokenDto);

    return ResponseEntity.ok(ApiResponse.success("카카오 로그인에 성공하였습니다.", loginData));
  }

  @Operation(summary = "JWT 재발급", description = "Refresh Token을 기반으로 Access Token을 재발급합니다.")
  @ApiResponseConstants.SuccessResponses
  @ApiResponseConstants.ClientErrorResponses
  @PostMapping("/token/reissue")
  public ResponseEntity<ApiResponse<Void>> reissueToken(
      @Parameter(description = "리프레시 토큰 쿠키") @CookieValue(value = "refreshToken", required = false)
          String refreshToken,
      HttpServletResponse response) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
    }

    try {
      OAuthTokenResponseDto newTokenDto = oAuthReissueTokenService.reissue(refreshToken);

      response.addHeader(
          HttpHeaders.SET_COOKIE,
          authCookieProvider.createAccessTokenCookie(newTokenDto.accessToken()).toString());

      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      throw new CustomException(GlobalErrorCode.TOKEN_REISSUE_FAILED);
    }
  }

  @Operation(summary = "로그아웃", description = "AccessToken을 블랙리스트에 등록하고 쿠키를 제거합니다.")
  @ApiResponseConstants.SuccessResponses
  @ApiResponseConstants.ClientErrorResponses
  @DeleteMapping("/{provider}/token")
  public ResponseEntity<ApiResponse<Void>> logout(
      @Parameter(description = "OAuth 제공자", example = "kakao") @PathVariable String provider,
      @Parameter(description = "액세스 토큰 쿠키") @CookieValue(value = "accessToken", required = false)
          String accessToken,
      @Parameter(description = "리프레시 토큰 쿠키") @CookieValue(value = "refreshToken", required = false)
          String refreshToken,
      HttpServletResponse response) {
    if (accessToken == null || accessToken.isBlank()) {
      throw new CustomException(
          MemberErrorCode.INVALID_LOGOUT_REQUEST, "accessToken 쿠키가 존재하지 않습니다.");
    }
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new CustomException(GlobalErrorCode.UNAUTHORIZED, "refreshToken 쿠키가 존재하지 않습니다.");
    }

    try {
      oAuthLoginService.logout(accessToken, refreshToken);

      response.addHeader(
          HttpHeaders.SET_COOKIE, authCookieProvider.clearAccessTokenCookie().toString());
      response.addHeader(
          HttpHeaders.SET_COOKIE, authCookieProvider.clearRefreshTokenCookie().toString());

      return ResponseEntity.ok(ApiResponse.success("로그아웃이 성공적으로 처리되었습니다."));
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      throw new CustomException(MemberErrorCode.LOGOUT_FAILED, "예상치 못한 오류로 로그아웃에 실패했습니다.");
    }
  }

  private void addLoginCookies(HttpServletResponse response, OAuthTokenResponseDto tokenDto) {
    response.addHeader(
        HttpHeaders.SET_COOKIE,
        authCookieProvider.createAccessTokenCookie(tokenDto.accessToken()).toString());
    response.addHeader(
        HttpHeaders.SET_COOKIE,
        authCookieProvider.createRefreshTokenCookie(tokenDto.refreshToken()).toString());
  }
}
