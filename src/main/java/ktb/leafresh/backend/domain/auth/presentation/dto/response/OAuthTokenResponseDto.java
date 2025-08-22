package ktb.leafresh.backend.domain.auth.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "OAuth 토큰 응답 데이터")
@Builder
public record OAuthTokenResponseDto(
    @Schema(description = "토큰 타입", example = "Bearer") String grantType,
    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,
    @Schema(description = "액세스 토큰 만료 시간 (초)", example = "3600") Long accessTokenExpiresIn,
    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken,
    @Schema(description = "회원 닉네임", example = "leafresh") String nickname,
    @Schema(description = "프로필 이미지 URL", example = "https://leafresh.io/profile.png")
        String imageUrl,
    @Schema(description = "OAuth 제공자 고유 ID", example = "1234567890") String providerId,
    @Schema(description = "이메일", example = "user@leafresh.io") String email) {}
