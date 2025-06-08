package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberNicknameCheckServiceTest {

    private MemberRepository memberRepository;
    private MemberNicknameCheckService service;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        service = new MemberNicknameCheckService(memberRepository);
    }

    @Test
    @DisplayName("중복된 닉네임이 존재하면 true 반환")
    void isDuplicated_true() {
        // given
        String nickname = "leafresh_유저";
        when(memberRepository.existsByNickname(nickname)).thenReturn(true);

        // when
        boolean result = service.isDuplicated(nickname);

        // then
        assertThat(result).isTrue();
        verify(memberRepository, times(1)).existsByNickname(nickname);
    }

    @Test
    @DisplayName("중복되지 않은 닉네임이면 false 반환")
    void isDuplicated_false() {
        // given
        String nickname = "새로운닉네임";
        when(memberRepository.existsByNickname(nickname)).thenReturn(false);

        // when
        boolean result = service.isDuplicated(nickname);

        // then
        assertThat(result).isFalse();
        verify(memberRepository, times(1)).existsByNickname(nickname);
    }
}
