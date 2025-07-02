package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.MemberLeafPointResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static ktb.leafresh.backend.support.fixture.MemberFixture.of;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberLeafPointReadServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberLeafPointReadService service;

    @Test
    @DisplayName("getCurrentLeafPoints_존재하는_회원이면_포인트를_반환한다")
    void getCurrentLeafPoints_withValidMember_returnsLeafPoints() {
        // given
        Long memberId = 1L;
        Member member = of("test@leafresh.com", "테스터");
        member.updateCurrentLeafPoints(1234);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        MemberLeafPointResponseDto result = service.getCurrentLeafPoints(memberId);

        // then
        assertThat(result.getCurrentLeafPoints()).isEqualTo(member.getCurrentLeafPoints());
        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    @DisplayName("getCurrentLeafPoints_존재하지않는_회원이면_예외를_던진다")
    void getCurrentLeafPoints_withInvalidMember_throwsException() {
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
