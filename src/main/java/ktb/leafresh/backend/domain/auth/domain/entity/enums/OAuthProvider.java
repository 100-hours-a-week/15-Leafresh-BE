package ktb.leafresh.backend.domain.auth.domain.entity.enums;

import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;

import java.util.Arrays;

public enum OAuthProvider {
  KAKAO,
  NAVER,
  GOOGLE;

  public static OAuthProvider from(String name) {
    return Arrays.stream(values())
        .filter(p -> p.name().equalsIgnoreCase(name))
        .findFirst()
        .orElseThrow(() -> new CustomException(MemberErrorCode.INVALID_PROVIDER));
  }
}
