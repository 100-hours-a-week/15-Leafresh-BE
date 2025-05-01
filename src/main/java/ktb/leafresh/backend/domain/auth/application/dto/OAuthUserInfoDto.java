package ktb.leafresh.backend.domain.auth.application.dto;

import ktb.leafresh.backend.domain.member.domain.entity.enums.LoginType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OAuthUserInfoDto {
    private LoginType provider;
    private String providerId;
    private String email;
    private String profileImageUrl;
}
