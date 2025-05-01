package ktb.leafresh.backend.domain.auth.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OAuth 회원가입 응답 데이터")
public record OAuthSignupResponseDto(
        @Schema(description = "회원 닉네임", example = "leafresh")
        String nickname,

        @Schema(description = "프로필 이미지 URL", example = "https://leafresh.io/profile.png")
        String imageUrl
) {}
