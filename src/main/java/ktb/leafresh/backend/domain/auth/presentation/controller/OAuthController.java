package ktb.leafresh.backend.domain.auth.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import ktb.leafresh.backend.domain.auth.application.service.OAuthLoginService;
import ktb.leafresh.backend.domain.auth.presentation.dto.response.OAuthLoginResponseDto;
import ktb.leafresh.backend.domain.auth.presentation.dto.response.OAuthTokenResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ErrorCode;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.response.ApiResponseConstants;
import ktb.leafresh.backend.global.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthLoginService oAuthLoginService;
    private final TokenBlacklistService tokenBlacklistService;

    @GetMapping("/success")
    public ResponseEntity<String> oauthSuccessPage() {
        return ResponseEntity.ok("<h1>카카오 로그인 성공</h1><p>쿠키 확인은 개발자 도구에서</p>");
    }

    @Operation(summary = "카카오 로그인 리다이렉트", description = "카카오 인증 페이지로 리다이렉트합니다.")
    @ApiResponseConstants.RedirectResponses
    @GetMapping("/{provider}")
    public ResponseEntity<Void> redirectToProvider(@PathVariable String provider) {
        if (!provider.equalsIgnoreCase("kakao")) {
            throw new CustomException(ErrorCode.INVALID_PROVIDER);
        }

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

        if (!provider.equalsIgnoreCase("kakao")) {
            throw new CustomException(ErrorCode.INVALID_PROVIDER);
        }

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
    @PostMapping("/{provider}/token")
    public ResponseEntity<ApiResponse<Void>> reissueToken(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response
    ) {
        OAuthTokenResponseDto tokenDto = oAuthLoginService.reissueToken(refreshToken);

        response.addHeader(HttpHeaders.SET_COOKIE, oAuthLoginService.createAccessTokenCookie(tokenDto).toString());

        return ResponseEntity.ok(ApiResponse.success("AccessToken이 재발급되었습니다."));
    }

    @Operation(summary = "로그아웃", description = "AccessToken을 블랙리스트에 등록하고 쿠키를 제거합니다.")
    @ApiResponseConstants.SuccessResponses
    @ApiResponseConstants.ClientErrorResponses
    @DeleteMapping("/{provider}/token")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue("accessToken") String accessToken,
            HttpServletResponse response
    ) {
        Long expireTime = oAuthLoginService.getTokenExpiration(accessToken);
        tokenBlacklistService.blacklistAccessToken(accessToken, expireTime);

        removeAccessTokenCookie(response);

        return ResponseEntity.ok(ApiResponse.success("로그아웃이 성공적으로 처리되었습니다."));
    }

    private void addLoginCookies(HttpServletResponse response, OAuthTokenResponseDto tokenDto) {
        response.addHeader(HttpHeaders.SET_COOKIE, oAuthLoginService.createAccessTokenCookie(tokenDto).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, oAuthLoginService.createRefreshTokenCookie(tokenDto).toString());
    }

    private void removeAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from("accessToken", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    }
}
