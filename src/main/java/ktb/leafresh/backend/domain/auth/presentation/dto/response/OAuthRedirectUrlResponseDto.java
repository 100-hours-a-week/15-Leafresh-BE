package ktb.leafresh.backend.domain.auth.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OAuth 리다이렉트 URL 응답 데이터")
public record OAuthRedirectUrlResponseDto(
    @Schema(
            description = "OAuth 인증 리다이렉트 URL",
            example = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=...")
        String redirectUrl) {}
