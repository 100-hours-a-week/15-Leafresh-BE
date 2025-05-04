package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberNicknameCheckService {

    private final MemberRepository memberRepository;

    public boolean isDuplicated(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }
}
