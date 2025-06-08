package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.MemberLeafPointResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static ktb.leafresh.backend.support.fixture.MemberFixture.of;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberLeafPointReadServiceTest {

    private MemberRepository memberRepository;
    private MemberLeafPointReadService service;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        service = new MemberLeafPointReadService(memberRepository);
    }

    @Test
    @DisplayName("회원의 현재 나뭇잎 포인트 조회 성공")
    void getCurrentLeafPoints_success() {
        // given
        Long memberId = 1L;
        Member member = of(memberId, "test@leafresh.com", "테스터");
        member.updateCurrentLeafPoints(1234);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        MemberLeafPointResponseDto result = service.getCurrentLeafPoints(memberId);

        // then
        assertThat(result.getCurrentLeafPoints()).isEqualTo(1234);
        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 조회 시 예외 발생")
    void getCurrentLeafPoints_memberNotFound() {
        // given
        Long memberId = 999L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when
        CustomException exception = catchThrowableOfType(
                () -> service.getCurrentLeafPoints(memberId),
                CustomException.class
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
        verify(memberRepository, times(1)).findById(memberId);
    }
}
