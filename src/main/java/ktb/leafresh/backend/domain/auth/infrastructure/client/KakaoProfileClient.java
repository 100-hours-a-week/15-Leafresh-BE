package ktb.leafresh.backend.domain.auth.infrastructure.client;

import ktb.leafresh.backend.domain.auth.application.dto.OAuthUserInfoDto;
import ktb.leafresh.backend.domain.auth.domain.entity.enums.OAuthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoProfileClient {

  private final WebClient webClient;

  private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

  public OAuthUserInfoDto getUserProfile(String accessToken) {
    return webClient
        .get()
        .uri(USER_INFO_URL)
        .headers(headers -> headers.setBearerAuth(accessToken))
        .retrieve()
        .bodyToMono(KakaoProfileResponse.class)
        .map(
            profile ->
                new OAuthUserInfoDto(
                    OAuthProvider.KAKAO,
                    profile.getId(),
                    profile.getKakaoAccountEmail(),
                    profile.getProfileImageUrl(),
                    profile.getNickname()))
        .blockOptional()
        .orElseThrow(() -> new IllegalStateException("카카오 사용자 정보 조회 실패"));
  }

  public record KakaoProfileResponse(Object id, Map<String, Object> kakao_account) {
    public String getId() {
      return id.toString();
    }

    public String getKakaoAccountEmail() {
      if (kakao_account == null) return null;
      return (String) kakao_account.get("email");
    }

    public String getProfileImageUrl() {
      if (kakao_account == null) return null;
      Map<String, Object> profile = (Map<String, Object>) kakao_account.get("profile");
      if (profile == null) return null;
      return (String) profile.get("profile_image_url");
    }

    public String getNickname() {
      if (kakao_account == null) return null;
      Map<String, Object> profile = (Map<String, Object>) kakao_account.get("profile");
      if (profile == null) return null;
      return (String) profile.get("nickname");
    }
  }
}
