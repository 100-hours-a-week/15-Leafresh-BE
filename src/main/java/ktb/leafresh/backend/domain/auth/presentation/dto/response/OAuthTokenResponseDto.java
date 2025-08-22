package ktb.leafresh.backend.domain.auth.presentation.dto.response;

import lombok.Builder;

@Builder
public record OAuthTokenResponseDto(
    String grantType,
    String accessToken,
    Long accessTokenExpiresIn,
    String refreshToken,
    String nickname,
    String imageUrl,
    String providerId,
    String email) {}
