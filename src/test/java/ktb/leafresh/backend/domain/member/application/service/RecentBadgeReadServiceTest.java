package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.MemberBadge;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.RecentBadgeListResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.support.fixture.BadgeFixture;
import ktb.leafresh.backend.support.fixture.MemberBadgeFixture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static ktb.leafresh.backend.support.fixture.MemberFixture.of;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecentBadgeReadServiceTest {

    private MemberRepository memberRepository;
    private MemberBadgeRepository memberBadgeRepository;
    private RecentBadgeReadService service;

    private Member member;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        memberBadgeRepository = mock(MemberBadgeRepository.class);
        service = new RecentBadgeReadService(memberRepository, memberBadgeRepository);

        member = of(1L, "test@leafresh.com", "테스터");
    }

    @Test
    @DisplayName("최근 뱃지를 정상적으로 조회한다")
    void getRecentBadges_success() {
        // given
        Badge badge = BadgeFixture.of(1L, "참가왕");
        MemberBadge memberBadge = MemberBadgeFixture.of(member, badge);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberBadgeRepository.findRecentBadgesByMemberId(1L, 3)).thenReturn(List.of(memberBadge));

        // when
        RecentBadgeListResponseDto response = service.getRecentBadges(1L, 3);

        // then
        assertThat(response.badges()).hasSize(1);
        assertThat(response.badges().get(0).name()).isEqualTo("참가왕");
        assertThat(response.badges().get(0).imageUrl()).contains("참가왕");
    }

    @Test
    @DisplayName("존재하지 않는 회원일 경우 예외를 던진다")
    void getRecentBadges_fail_memberNotFound() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        CustomException exception = catchThrowableOfType(() -> service.getRecentBadges(1L, 3), CustomException.class);

        // then
        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("최근 뱃지 조회 중 예기치 못한 예외 발생 시 BADGE_QUERY_FAILED 예외 반환")
    void getRecentBadges_unexpectedError() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberBadgeRepository.findRecentBadgesByMemberId(anyLong(), anyInt()))
                .thenThrow(new RuntimeException("DB Error"));

        // when
        CustomException exception = catchThrowableOfType(() -> service.getRecentBadges(1L, 3), CustomException.class);

        // then
        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.BADGE_QUERY_FAILED);
    }
}
