package ktb.leafresh.backend.domain.auth.presentation.dto.result;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.auth.presentation.dto.response.OAuthSignupResponseDto;
import ktb.leafresh.backend.global.security.TokenDto;
import lombok.Builder;

@Schema(description = "OAuth 회원가입 결과 데이터")
@Builder
public record OAuthSignupResult(
    @Schema(description = "회원가입 응답 데이터") OAuthSignupResponseDto signupResponse,
    @Schema(description = "토큰 정보") TokenDto tokenDto) {}
