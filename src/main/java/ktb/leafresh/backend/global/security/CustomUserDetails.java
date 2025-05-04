package ktb.leafresh.backend.global.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long memberId;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Long memberId, Collection<? extends GrantedAuthority> authorities) {
        this.memberId = memberId;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return ""; // 소셜 로그인이라면 password는 빈 문자열로
    }

    @Override
    public String getUsername() {
        return memberId.toString(); // username으로 memberId 사용
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
