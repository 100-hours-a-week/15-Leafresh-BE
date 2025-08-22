package ktb.leafresh.backend.domain.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.auth.domain.entity.enums.OAuthProvider;
import ktb.leafresh.backend.domain.member.domain.entity.enums.LoginType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "OAuth 사용자 정보 DTO")
@Getter
@AllArgsConstructor
public class OAuthUserInfoDto {
  @Schema(description = "OAuth 제공자", example = "KAKAO")
  private OAuthProvider provider;

  @Schema(description = "OAuth 제공자 고유 ID", example = "1234567890")
  private String providerId;

  @Schema(description = "이메일", example = "user@leafresh.io")
  private String email;

  @Schema(description = "프로필 이미지 URL", example = "https://leafresh.io/profile.png")
  private String profileImageUrl;

  @Schema(description = "닉네임", example = "leafresh")
  private String nickname;
}
