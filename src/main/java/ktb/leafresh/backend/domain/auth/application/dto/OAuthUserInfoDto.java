package ktb.leafresh.backend.domain.auth.application.dto;

import ktb.leafresh.backend.domain.auth.domain.entity.enums.OAuthProvider;
import ktb.leafresh.backend.domain.member.domain.entity.enums.LoginType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OAuthUserInfoDto {
    private OAuthProvider provider;
    private String providerId;
    private String email;
    private String profileImageUrl;
}
