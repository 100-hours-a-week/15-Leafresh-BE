package ktb.leafresh.backend.domain.member.application.service.policy;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.enums.BadgeType;
import ktb.leafresh.backend.domain.member.infrastructure.repository.BadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.support.fixture.BadgeFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TotalVerificationCountBadgePolicyTest {

    private GroupChallengeVerificationRepository groupRepo;
    private PersonalChallengeVerificationRepository personalRepo;
    private BadgeRepository badgeRepository;
    private MemberBadgeRepository memberBadgeRepository;
    private TotalVerificationCountBadgePolicy policy;

    @BeforeEach
    void setUp() {
        groupRepo = mock(GroupChallengeVerificationRepository.class);
        personalRepo = mock(PersonalChallengeVerificationRepository.class);
        badgeRepository = mock(BadgeRepository.class);
        memberBadgeRepository = mock(MemberBadgeRepository.class);
        policy = new TotalVerificationCountBadgePolicy(groupRepo, personalRepo, badgeRepository, memberBadgeRepository);
    }

    @Test
    @DisplayName("총 10회 인증 성공 → '첫 발자국' 뱃지 지급")
    void totalCount10_ThenGrantBeginnerBadge() {
        Member member = MemberFixture.of();
        Badge badge = BadgeFixture.of(1L, "첫 발자국", BadgeType.TOTAL);

        when(groupRepo.countByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(6L);
        when(personalRepo.countTotalByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(4L);
        when(badgeRepository.findByName("첫 발자국")).thenReturn(Optional.of(badge));
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge)).thenReturn(false);

        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        assertThat(result).extracting(Badge::getName).containsExactly("첫 발자국");
    }

    @Test
    @DisplayName("총 30회 인증 성공 + 10회 뱃지 보유 → '실천 중급자' 뱃지만 지급")
    void totalCount30_WithBeginnerBadgeOwned_ThenGrantOneBadge() {
        Member member = MemberFixture.of();
        Badge badge10 = BadgeFixture.of(1L, "첫 발자국", BadgeType.TOTAL);
        Badge badge30 = BadgeFixture.of(2L, "실천 중급자", BadgeType.TOTAL);

        when(groupRepo.countByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(15L);
        when(personalRepo.countTotalByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(15L);

        when(badgeRepository.findByName("첫 발자국")).thenReturn(Optional.of(badge10));
        when(badgeRepository.findByName("실천 중급자")).thenReturn(Optional.of(badge30));
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge10)).thenReturn(true);
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge30)).thenReturn(false);

        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        assertThat(result).extracting(Badge::getName).containsExactly("실천 중급자");
    }

    @Test
    @DisplayName("총 30회 인증 성공 → 2개 뱃지 지급")
    void totalCount30_ThenGrantTwoBadges() {
        Member member = MemberFixture.of();

        when(groupRepo.countByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(20L);
        when(personalRepo.countTotalByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(10L);

        Badge badge10 = BadgeFixture.of(1L, "첫 발자국", BadgeType.TOTAL);
        Badge badge30 = BadgeFixture.of(2L, "실천 중급자", BadgeType.TOTAL);

        when(badgeRepository.findByName("첫 발자국")).thenReturn(Optional.of(badge10));
        when(badgeRepository.findByName("실천 중급자")).thenReturn(Optional.of(badge30));
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge10)).thenReturn(false);
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge30)).thenReturn(false);

        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        assertThat(result).extracting(Badge::getName).containsExactlyInAnyOrder("첫 발자국", "실천 중급자");
    }

    @Test
    @DisplayName("총 50회 인증 성공 + 10, 30회 뱃지 보유 → '지속가능 파이터' 뱃지만 지급")
    void totalCount50_With10And30BadgeOwned_ThenGrantOneBadge() {
        Member member = MemberFixture.of();
        Badge badge10 = BadgeFixture.of(1L, "첫 발자국", BadgeType.TOTAL);
        Badge badge30 = BadgeFixture.of(2L, "실천 중급자", BadgeType.TOTAL);
        Badge badge50 = BadgeFixture.of(3L, "지속가능 파이터", BadgeType.TOTAL);

        when(groupRepo.countByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(30L);
        when(personalRepo.countTotalByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(20L);

        when(badgeRepository.findByName("첫 발자국")).thenReturn(Optional.of(badge10));
        when(badgeRepository.findByName("실천 중급자")).thenReturn(Optional.of(badge30));
        when(badgeRepository.findByName("지속가능 파이터")).thenReturn(Optional.of(badge50));
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge10)).thenReturn(true);
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge30)).thenReturn(true);
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge50)).thenReturn(false);

        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        assertThat(result).extracting(Badge::getName).containsExactly("지속가능 파이터");
    }

    @Test
    @DisplayName("총 100회 인증 성공 → 4개 뱃지 지급")
    void totalCount100_ThenGrantFourBadges() {
        Member member = MemberFixture.of();
        List<Badge> badges = List.of(
                BadgeFixture.of(1L, "첫 발자국", BadgeType.TOTAL),
                BadgeFixture.of(2L, "실천 중급자", BadgeType.TOTAL),
                BadgeFixture.of(3L, "지속가능 파이터", BadgeType.TOTAL),
                BadgeFixture.of(4L, "그린 마스터", BadgeType.TOTAL)
        );

        when(groupRepo.countByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(60L);
        when(personalRepo.countTotalByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(40L);

        for (Badge badge : badges) {
            when(badgeRepository.findByName(badge.getName())).thenReturn(Optional.of(badge));
            when(memberBadgeRepository.existsByMemberAndBadge(member, badge)).thenReturn(false);
        }

        List<Badge> result = policy.evaluateAndGetNewBadges(member);
        assertThat(result).extracting(Badge::getName).containsExactlyInAnyOrder("첫 발자국", "실천 중급자", "지속가능 파이터", "그린 마스터");
    }

    @Test
    @DisplayName("이미 보유한 뱃지는 중복 지급되지 않음")
    void alreadyOwnedBadge_ThenSkip() {
        Member member = MemberFixture.of();
        Badge badge = BadgeFixture.of(1L, "첫 발자국", BadgeType.TOTAL);

        when(groupRepo.countByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(8L);
        when(personalRepo.countTotalByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(5L);
        when(badgeRepository.findByName("첫 발자국")).thenReturn(Optional.of(badge));
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge)).thenReturn(true);

        List<Badge> result = policy.evaluateAndGetNewBadges(member);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("성공 횟수 부족 시 뱃지 없음")
    void totalCountUnder10_ThenNoBadge() {
        Member member = MemberFixture.of();

        when(groupRepo.countByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(3L);
        when(personalRepo.countTotalByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(5L);

        List<Badge> result = policy.evaluateAndGetNewBadges(member);
        assertThat(result).isEmpty();
    }
}
