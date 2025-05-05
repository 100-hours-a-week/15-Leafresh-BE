package ktb.leafresh.backend.domain.auth.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import ktb.leafresh.backend.domain.auth.application.service.oauth.OAuthLoginService;
import ktb.leafresh.backend.domain.auth.application.service.oauth.OAuthReissueTokenService;
import ktb.leafresh.backend.domain.auth.domain.entity.enums.OAuthProvider;
import ktb.leafresh.backend.domain.auth.presentation.dto.response.OAuthLoginResponseDto;
import ktb.leafresh.backend.domain.auth.presentation.dto.response.OAuthTokenResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.response.ApiResponseConstants;
import ktb.leafresh.backend.global.security.AuthCookieProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthLoginService oAuthLoginService;
    private final OAuthReissueTokenService oAuthReissueTokenService;
    private final AuthCookieProvider authCookieProvider;

    @GetMapping("/success")
    public ResponseEntity<String> oauthSuccessPage() {
        return ResponseEntity.ok("<h1>카카오 로그인 성공</h1><p>쿠키 확인은 개발자 도구에서</p>");
    }

    @Operation(summary = "카카오 로그인 리다이렉트", description = "카카오 인증 페이지로 리다이렉트합니다.")
    @ApiResponseConstants.RedirectResponses
    @GetMapping("/{provider}")
    public ResponseEntity<Void> redirectToProvider(@PathVariable String provider) {
        OAuthProvider providerEnum = OAuthProvider.from(provider);

        String redirectUrl = oAuthLoginService.getRedirectUrl();
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    @Operation(summary = "카카오 로그인 콜백", description = "인가 코드를 받아 JWT를 발급하고 쿠키에 저장하며 사용자 정보를 반환합니다.")
    @ApiResponseConstants.SuccessResponses
    @ApiResponseConstants.ClientErrorResponses
    @ApiResponseConstants.ServerErrorResponses
    @GetMapping("/{provider}/callback")
    public ResponseEntity<ApiResponse<OAuthLoginResponseDto>> kakaoCallback(
            @PathVariable String provider,
            @RequestParam String code,
            HttpServletResponse response
    ) {
        log.info("인가 코드 수신 - code={}", code);

        OAuthProvider providerEnum = OAuthProvider.from(provider);

        OAuthTokenResponseDto tokenDto = oAuthLoginService.loginWithKakao(code);
        log.info("카카오 로그인 토큰 발급 완료 - accessToken={}, refreshToken={}",
                tokenDto.accessToken(), tokenDto.refreshToken());

        OAuthLoginResponseDto loginData = new OAuthLoginResponseDto(
                tokenDto.accessToken() != null,
                tokenDto.email(),
                tokenDto.nickname(),
                tokenDto.imageUrl()
        );

        // 신규 회원은 쿠키 발급 생략
        if (tokenDto.accessToken() == null || tokenDto.accessTokenExpiresIn() == null) {
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
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
        }

        OAuthTokenResponseDto newTokenDto = oAuthReissueTokenService.reissue(refreshToken);

        response.addHeader(HttpHeaders.SET_COOKIE,
                authCookieProvider.createAccessTokenCookie(newTokenDto.accessToken(), newTokenDto.accessTokenExpiresIn()).toString());

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();  // 204: 응답 본문 없음, 쿠키만 발급
    }

    @Operation(summary = "로그아웃", description = "AccessToken을 블랙리스트에 등록하고 쿠키를 제거합니다.")
    @ApiResponseConstants.SuccessResponses
    @ApiResponseConstants.ClientErrorResponses
    @DeleteMapping("/{provider}/token")
    public ResponseEntity<ApiResponse<Void>> logout(
            @PathVariable String provider,
            @CookieValue("accessToken") String accessToken,
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response
    ) {
        oAuthLoginService.logout(accessToken, refreshToken);

        response.addHeader(HttpHeaders.SET_COOKIE, authCookieProvider.clearAccessTokenCookie().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieProvider.clearRefreshTokenCookie().toString());

        return ResponseEntity.ok(ApiResponse.success("로그아웃이 성공적으로 처리되었습니다."));
    }

    private void addLoginCookies(HttpServletResponse response, OAuthTokenResponseDto tokenDto) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                authCookieProvider.createAccessTokenCookie(tokenDto.accessToken(), tokenDto.accessTokenExpiresIn()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                authCookieProvider.createRefreshTokenCookie(tokenDto.refreshToken()).toString());
    }
}
