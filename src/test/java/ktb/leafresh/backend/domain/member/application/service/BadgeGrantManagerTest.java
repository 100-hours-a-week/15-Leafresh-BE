package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.MemberBadge;
import ktb.leafresh.backend.domain.member.domain.service.policy.BadgeGrantPolicy;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
import ktb.leafresh.backend.support.fixture.BadgeFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BadgeGrantManagerTest {

    private MemberBadgeRepository memberBadgeRepository;
    private BadgeGrantPolicy policy1;
    private BadgeGrantPolicy policy2;
    private BadgeGrantManager badgeGrantManager;

    @BeforeEach
    void setUp() {
        memberBadgeRepository = mock(MemberBadgeRepository.class);
        policy1 = mock(BadgeGrantPolicy.class);
        policy2 = mock(BadgeGrantPolicy.class);
        badgeGrantManager = new BadgeGrantManager(List.of(policy1, policy2), memberBadgeRepository);
    }

    @Test
    @DisplayName("모든 정책에서 나온 새 뱃지들을 저장한다")
    void evaluateAllAndGrant_SaveNewBadgesFromAllPolicies() {
        // Given
        Member member = MemberFixture.of();
        Badge badge1 = BadgeFixture.of(1L, "첫 발자국");
        Badge badge2 = BadgeFixture.of(2L, "지속가능 파이터");

        when(policy1.evaluateAndGetNewBadges(member)).thenReturn(List.of(badge1));
        when(policy2.evaluateAndGetNewBadges(member)).thenReturn(List.of(badge2));

        when(memberBadgeRepository.existsByMemberAndBadge(member, badge1)).thenReturn(false);
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge2)).thenReturn(false);

        ArgumentCaptor<MemberBadge> captor = ArgumentCaptor.forClass(MemberBadge.class);

        // When
        badgeGrantManager.evaluateAllAndGrant(member);

        // Then
        verify(memberBadgeRepository, times(2)).save(captor.capture());
        List<MemberBadge> saved = captor.getAllValues();
        assertThat(saved).extracting(mb -> mb.getBadge().getName())
                .containsExactlyInAnyOrder("첫 발자국", "지속가능 파이터");
    }

    @Test
    @DisplayName("이미 보유한 뱃지는 저장하지 않는다")
    void evaluateAllAndGrant_SkipAlreadyOwnedBadges() {
        // Given
        Member member = MemberFixture.of();
        Badge badge = BadgeFixture.of(1L, "첫 발자국");

        when(policy1.evaluateAndGetNewBadges(member)).thenReturn(List.of(badge));
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge)).thenReturn(true); // 이미 보유

        // When
        badgeGrantManager.evaluateAllAndGrant(member);

        // Then
        verify(memberBadgeRepository, never()).save(any());
    }

    @Test
    @DisplayName("정책에서 반환된 뱃지가 없을 경우 저장하지 않는다")
    void evaluateAllAndGrant_NoBadgeToGrant() {
        // Given
        Member member = MemberFixture.of();

        when(policy1.evaluateAndGetNewBadges(member)).thenReturn(List.of());
        when(policy2.evaluateAndGetNewBadges(member)).thenReturn(List.of());

        // When
        badgeGrantManager.evaluateAllAndGrant(member);

        // Then
        verify(memberBadgeRepository, never()).save(any());
    }
}
