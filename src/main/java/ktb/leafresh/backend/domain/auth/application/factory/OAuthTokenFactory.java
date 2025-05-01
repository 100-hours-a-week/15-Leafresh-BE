package ktb.leafresh.backend.domain.auth.application.factory;

import ktb.leafresh.backend.domain.auth.domain.entity.OAuth;
import ktb.leafresh.backend.domain.auth.presentation.dto.response.OAuthTokenResponseDto;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.security.TokenDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthTokenFactory {

    public OAuthTokenResponseDto create(Member member, TokenDto tokenDto) {
        String providerId = member.getAuths().stream()
                .filter(auth -> auth.getProvider() == member.getLoginType()) // loginType 기반으로 추출
                .findFirst()
                .map(OAuth::getProviderId)
                .orElse(null);

        return OAuthTokenResponseDto.builder()
                .grantType(tokenDto.getGrantType())
                .accessToken(tokenDto.getAccessToken())
                .accessTokenExpiresIn(tokenDto.getAccessTokenExpiresIn())
                .refreshToken(tokenDto.getRefreshToken())
                .nickname(member.getNickname())
                .imageUrl(member.getImageUrl())
                .providerId(providerId)
                .email(member.getEmail())
                .build();
    }
}
