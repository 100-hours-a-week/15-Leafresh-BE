package ktb.leafresh.backend.global.security;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 숫자면 memberId로 판단 (JWT 인증 흐름)
        if (username.matches("\\d+")) {
            Long memberId = Long.parseLong(username);
            return memberRepository.findById(memberId)
                    .map(this::createUserDetails)
                    .orElseThrow(() -> new UsernameNotFoundException("ID " + memberId + " -> 데이터베이스에서 찾을 수 없습니다."));
        }

        // 아니면 email로 판단 (로그인 시)
        return memberRepository.findByEmail(username)
                .map(this::createUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("Email " + username + " -> 데이터베이스에서 찾을 수 없습니다."));
    }

    private UserDetails createUserDetails(Member member) {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(member.getRole().name());
        return new CustomUserDetails(member.getId(), Collections.singleton(grantedAuthority));
    }
}
