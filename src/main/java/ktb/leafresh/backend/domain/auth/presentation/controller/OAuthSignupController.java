package ktb.leafresh.backend.domain.auth.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletResponse;
import ktb.leafresh.backend.domain.auth.application.service.oauth.OAuthSignupService;
import ktb.leafresh.backend.domain.auth.presentation.dto.request.OAuthSignupRequestDto;
import ktb.leafresh.backend.domain.auth.presentation.dto.response.OAuthLoginResponseDto;
import ktb.leafresh.backend.domain.auth.presentation.dto.response.OAuthSignupResponseDto;
import ktb.leafresh.backend.domain.auth.presentation.dto.result.OAuthSignupResult;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.AuthCookieProvider;
import ktb.leafresh.backend.global.security.TokenDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "OAuth 회원가입", description = "OAuth 인증 이후 회원가입 API")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class OAuthSignupController {

    private final OAuthSignupService signupService;
    private final AuthCookieProvider authCookieProvider;

    @Operation(
            summary = "OAuth 회원가입",
            description = "닉네임이 중복되지 않으면 회원 정보를 저장하고 토큰을 발급합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200", description = "회원가입 성공",
                            content = @Content(schema = @Schema(implementation = OAuthLoginResponseDto.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 사용 중인 닉네임입니다."),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류입니다.")
            }
    )
    @PostMapping
    public ResponseEntity<ApiResponse<OAuthSignupResponseDto>> signup(
            @RequestBody OAuthSignupRequestDto request,
            HttpServletResponse response
    ) {
        log.info("회원가입 요청 수신 - email={}, provider={}, providerId={}, nickname={}",
                request.email(), request.provider(), request.provider().id(), request.nickname());

        OAuthSignupResult result = signupService.signup(request);
        TokenDto tokenDto = result.tokenDto();

        response.addHeader(HttpHeaders.SET_COOKIE,
                authCookieProvider.createAccessTokenCookie(tokenDto.getAccessToken()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                authCookieProvider.createRefreshTokenCookie(tokenDto.getRefreshToken()).toString());

        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", result.signupResponse()));
    }
}
