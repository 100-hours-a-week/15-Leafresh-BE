package ktb.leafresh.backend.domain.auth.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OAuth 로그인 응답 데이터")
public record OAuthLoginResponseDto(
    @Schema(description = "회원 여부", example = "true") boolean isMember,
    @Schema(description = "이메일", example = "user@leafresh.io") String email,
    @Schema(description = "회원 닉네임", example = "leafresh") String nickname,
    @Schema(description = "프로필 이미지 URL", example = "https://leafresh.io/profile.png")
        String imageUrl) {}
