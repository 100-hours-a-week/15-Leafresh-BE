package ktb.leafresh.backend.domain.auth.infrastructure.client;

import ktb.leafresh.backend.domain.auth.application.dto.OAuthUserInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthKakaoService {

    private final KakaoTokenClient kakaoTokenClient;
    private final KakaoProfileClient kakaoProfileClient;

    public OAuthUserInfoDto getUserInfo(String authorizationCode) {
        String accessToken = kakaoTokenClient.getAccessToken(authorizationCode);
        return kakaoProfileClient.getUserProfile(accessToken);
    }
}
