package ktb.leafresh.backend.domain.auth.presentation.dto.result;

import ktb.leafresh.backend.domain.auth.presentation.dto.response.OAuthSignupResponseDto;
import ktb.leafresh.backend.global.security.TokenDto;
import lombok.Builder;

@Builder
public record OAuthSignupResult(
        OAuthSignupResponseDto signupResponse,
        TokenDto tokenDto
) {}
