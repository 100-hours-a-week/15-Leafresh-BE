package ktb.leafresh.backend.domain.member.application.service.policy;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.enums.BadgeType;
import ktb.leafresh.backend.domain.member.infrastructure.repository.BadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
import ktb.leafresh.backend.support.fixture.BadgeFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PersonalChallengeStreakBadgePolicyTest {

    private PersonalChallengeVerificationRepository verificationRepository;
    private BadgeRepository badgeRepository;
    private MemberBadgeRepository memberBadgeRepository;
    private PersonalChallengeStreakBadgePolicy policy;

    @BeforeEach
    void setUp() {
        verificationRepository = mock(PersonalChallengeVerificationRepository.class);
        badgeRepository = mock(BadgeRepository.class);
        memberBadgeRepository = mock(MemberBadgeRepository.class);
        policy = new PersonalChallengeStreakBadgePolicy(verificationRepository, badgeRepository, memberBadgeRepository);
    }

    @Test
    @DisplayName("연속 인증 일수에 따라 여러 뱃지가 지급된다")
    void evaluateAndGetNewBadges_Given30DaysStreak_ThenMultipleBadgesGranted() {
        // Given
        Member member = MemberFixture.of();
        when(verificationRepository.countConsecutiveSuccessDays(member.getId()))
                .thenReturn(30);

        Badge badge1 = BadgeFixture.of(1L, "새싹 실천러", BadgeType.PERSONAL);
        Badge badge2 = BadgeFixture.of(2L, "일주일의 습관", BadgeType.PERSONAL);
        Badge badge3 = BadgeFixture.of(3L, "반달 에코러", BadgeType.PERSONAL);
        Badge badge4 = BadgeFixture.of(4L, "한 달 챌린지 완주자", BadgeType.PERSONAL);

        when(badgeRepository.findByName("새싹 실천러")).thenReturn(Optional.of(badge1));
        when(badgeRepository.findByName("일주일의 습관")).thenReturn(Optional.of(badge2));
        when(badgeRepository.findByName("반달 에코러")).thenReturn(Optional.of(badge3));
        when(badgeRepository.findByName("한 달 챌린지 완주자")).thenReturn(Optional.of(badge4));

        when(memberBadgeRepository.existsByMemberAndBadge(member, badge1)).thenReturn(false);
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge2)).thenReturn(false);
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge3)).thenReturn(false);
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge4)).thenReturn(false);

        // When
        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        // Then
        assertThat(result).containsExactlyInAnyOrder(badge1, badge2, badge3, badge4);
    }

    @Test
    @DisplayName("이미 보유한 뱃지는 지급되지 않는다")
    void evaluateAndGetNewBadges_AlreadyOwned_ThenSkip() {
        // Given
        Member member = MemberFixture.of();
        when(verificationRepository.countConsecutiveSuccessDays(member.getId()))
                .thenReturn(14);

        Badge badge = BadgeFixture.of(5L, "반달 에코러", BadgeType.PERSONAL);

        when(badgeRepository.findByName("새싹 실천러")).thenReturn(Optional.empty()); // 못 찾는 경우
        when(badgeRepository.findByName("일주일의 습관")).thenReturn(Optional.empty());
        when(badgeRepository.findByName("반달 에코러")).thenReturn(Optional.of(badge));
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge)).thenReturn(true);

        // When
        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("연속 인증 일수가 부족하면 뱃지를 지급하지 않는다")
    void evaluateAndGetNewBadges_StreakTooShort_ThenNoBadge() {
        // Given
        Member member = MemberFixture.of();
        when(verificationRepository.countConsecutiveSuccessDays(member.getId()))
                .thenReturn(1); // insufficient

        // When
        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        // Then
        assertThat(result).isEmpty();
    }
}
