package ktb.leafresh.backend.domain.auth.infrastructure.client;

import ktb.leafresh.backend.domain.auth.application.dto.OAuthUserInfoDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthKakaoService {

    private final KakaoTokenClient kakaoTokenClient;
    private final KakaoProfileClient kakaoProfileClient;

    public OAuthUserInfoDto getUserInfo(String authorizationCode, String redirectUri) {
        try {
            String accessToken = kakaoTokenClient.getAccessToken(authorizationCode, redirectUri);
            OAuthUserInfoDto kakaoUser = kakaoProfileClient.getUserProfile(accessToken);

            // 디폴트 이미지 적용
            return new OAuthUserInfoDto(
                    kakaoUser.getProvider(),
                    kakaoUser.getProviderId(),
                    kakaoUser.getEmail(),
                    "https://storage.googleapis.com/leafresh-images/init/user_icon.png",
                    kakaoUser.getNickname()
            );
        } catch (Exception e) {
            throw new CustomException(MemberErrorCode.KAKAO_TOKEN_ISSUE_FAILED, e.getMessage());
        }
    }
}
