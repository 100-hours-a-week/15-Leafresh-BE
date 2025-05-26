package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.BadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.BadgeListResponseDto;
import ktb.leafresh.backend.domain.member.presentation.dto.response.BadgeResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.support.fixture.BadgeFixture;
import ktb.leafresh.backend.support.fixture.MemberBadgeFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BadgeReadServiceTest {

    private BadgeRepository badgeRepository;
    private MemberRepository memberRepository;
    private BadgeReadService badgeReadService;

    @BeforeEach
    void setUp() {
        badgeRepository = mock(BadgeRepository.class);
        memberRepository = mock(MemberRepository.class);
        badgeReadService = new BadgeReadService(badgeRepository, memberRepository);
    }

    @Test
    @DisplayName("성공: 획득한 뱃지와 잠긴 뱃지를 구분해 반환한다")
    void getAllBadges_GivenValidMember_WhenSomeBadgesAcquired_ThenReturnWithLockStatus() {
        // Given
        Long memberId = 1L;
        Member member = MemberFixture.of(memberId, "tester@leafresh.com", "테스터");

        Badge acquiredBadge = BadgeFixture.of(1L, "습지 전도사");
        Badge lockedBadge = BadgeFixture.of(2L, "지속가능 전도사");

        member.getMemberBadges().add(MemberBadgeFixture.of(member, acquiredBadge));

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(badgeRepository.findAll()).thenReturn(List.of(acquiredBadge, lockedBadge));

        // When
        BadgeListResponseDto response = badgeReadService.getAllBadges(memberId);

        // Then
        List<BadgeResponseDto> eventBadges = response.getBadges().get("event");
        assertThat(eventBadges).hasSize(2);

        assertThat(eventBadges)
                .anySatisfy(badge -> assertThat(badge.isLocked()).isFalse())
                .anySatisfy(badge -> assertThat(badge.isLocked()).isTrue());
    }

    @Test
    @DisplayName("실패: 존재하지 않는 회원 ID일 경우 예외를 던진다")
    void getAllBadges_WhenMemberNotFound_ThenThrow404() {
        // Given
        Long memberId = 999L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // Expect
        assertThatThrownBy(() -> badgeReadService.getAllBadges(memberId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("실패: 뱃지 목록이 비어 있을 경우 예외를 던진다")
    void getAllBadges_WhenBadgesEmpty_ThenThrow500() {
        // Given
        Member member = MemberFixture.of();
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(badgeRepository.findAll()).thenReturn(Collections.emptyList());

        // Expect
        assertThatThrownBy(() -> badgeReadService.getAllBadges(member.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(MemberErrorCode.BADGE_QUERY_FAILED.getMessage());
    }
}
