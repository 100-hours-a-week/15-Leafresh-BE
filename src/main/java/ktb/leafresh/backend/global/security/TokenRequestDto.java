package ktb.leafresh.backend.global.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 현재 컨트롤러에서는 @CookieValue로 직접 파라미터를 받고 있어 사용하지 않음
 */
@Getter
@Setter
@NoArgsConstructor
public class TokenRequestDto {

    private String accessToken;
    private String refreshToken;
}
