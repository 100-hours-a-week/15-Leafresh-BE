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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TotalVerificationCountBadgePolicy 테스트")
class TotalVerificationCountBadgePolicyTest {

    @Mock
    private GroupChallengeVerificationRepository groupRepo;

    @Mock
    private PersonalChallengeVerificationRepository personalRepo;

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private MemberBadgeRepository memberBadgeRepository;

    @InjectMocks
    private TotalVerificationCountBadgePolicy policy;

    private final Member member = MemberFixture.of();

    @Test
    @DisplayName("총 10회 인증 성공 시 '첫 발자국' 뱃지 지급")
    void totalVerificationCount10_grantsFirstStepBadge() {
        // given
        Badge badge = BadgeFixture.of("첫 발자국", BadgeType.TOTAL);

        when(groupRepo.countByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(6L);
        when(personalRepo.countTotalByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(4L);
        when(badgeRepository.findByName(badge.getName())).thenReturn(Optional.of(badge));
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge)).thenReturn(false);

        // when
        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        // then
        assertThat(result).containsExactly(badge);
    }

    @Test
    @DisplayName("총 30회 인증 + 10회 뱃지 보유 시 '실천 중급자' 뱃지만 지급")
    void totalVerificationCount30_withFirstBadgeOwned_grantsOnlyIntermediateBadge() {
        // given
        Badge badge10 = BadgeFixture.of("첫 발자국", BadgeType.TOTAL);
        Badge badge30 = BadgeFixture.of("실천 중급자", BadgeType.TOTAL);

        when(groupRepo.countByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(15L);
        when(personalRepo.countTotalByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(15L);
        when(badgeRepository.findByName(badge10.getName())).thenReturn(Optional.of(badge10));
        when(badgeRepository.findByName(badge30.getName())).thenReturn(Optional.of(badge30));
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge10)).thenReturn(true);
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge30)).thenReturn(false);

        // when
        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        // then
        assertThat(result).containsExactly(badge30);
    }

    @Test
    @DisplayName("총 30회 인증 성공 시 2개 뱃지 지급")
    void totalVerificationCount30_grantsTwoBadges() {
        // given
        Badge badge10 = BadgeFixture.of("첫 발자국", BadgeType.TOTAL);
        Badge badge30 = BadgeFixture.of("실천 중급자", BadgeType.TOTAL);

        when(groupRepo.countByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(20L);
        when(personalRepo.countTotalByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(10L);
        when(badgeRepository.findByName(badge10.getName())).thenReturn(Optional.of(badge10));
        when(badgeRepository.findByName(badge30.getName())).thenReturn(Optional.of(badge30));
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge10)).thenReturn(false);
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge30)).thenReturn(false);

        // when
        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        // then
        assertThat(result).containsExactlyInAnyOrder(badge10, badge30);
    }

    @Test
    @DisplayName("총 50회 인증 + 이전 뱃지 보유 시 '지속가능 파이터'만 지급")
    void totalVerificationCount50_with10And30Owned_grantsOnlyFighterBadge() {
        // given
        Badge badge10 = BadgeFixture.of("첫 발자국", BadgeType.TOTAL);
        Badge badge30 = BadgeFixture.of("실천 중급자", BadgeType.TOTAL);
        Badge badge50 = BadgeFixture.of("지속가능 파이터", BadgeType.TOTAL);

        when(groupRepo.countByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(30L);
        when(personalRepo.countTotalByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(20L);
        when(badgeRepository.findByName(badge10.getName())).thenReturn(Optional.of(badge10));
        when(badgeRepository.findByName(badge30.getName())).thenReturn(Optional.of(badge30));
        when(badgeRepository.findByName(badge50.getName())).thenReturn(Optional.of(badge50));
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge10)).thenReturn(true);
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge30)).thenReturn(true);
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge50)).thenReturn(false);

        // when
        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        // then
        assertThat(result).containsExactly(badge50);
    }

    @Test
    @DisplayName("총 100회 인증 시 모든 뱃지 지급")
    void totalVerificationCount100_grantsAllBadges() {
        // given
        List<Badge> badges = List.of(
                BadgeFixture.of("첫 발자국", BadgeType.TOTAL),
                BadgeFixture.of("실천 중급자", BadgeType.TOTAL),
                BadgeFixture.of("지속가능 파이터", BadgeType.TOTAL),
                BadgeFixture.of("그린 마스터", BadgeType.TOTAL)
        );

        when(groupRepo.countByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(60L);
        when(personalRepo.countTotalByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(40L);

        for (Badge badge : badges) {
            when(badgeRepository.findByName(badge.getName())).thenReturn(Optional.of(badge));
            when(memberBadgeRepository.existsByMemberAndBadge(member, badge)).thenReturn(false);
        }

        // when
        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        // then
        assertThat(result).containsExactlyInAnyOrderElementsOf(badges);
    }

    @Test
    @DisplayName("이미 보유한 뱃지는 지급되지 않음")
    void alreadyOwnedBadge_skipsThatBadge() {
        // given
        Badge badge = BadgeFixture.of("첫 발자국", BadgeType.TOTAL);

        when(groupRepo.countByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(8L);
        when(personalRepo.countTotalByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(5L);
        when(badgeRepository.findByName(badge.getName())).thenReturn(Optional.of(badge));
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge)).thenReturn(true);

        // when
        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        // then
        assertThat(result).doesNotContain(badge).isEmpty();
    }

    @Test
    @DisplayName("10회 미만 인증 시 뱃지 없음")
    void under10VerificationCount_returnsNoBadge() {
        // given
        when(groupRepo.countByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(3L);
        when(personalRepo.countTotalByMemberIdAndStatus(member.getId(), ChallengeStatus.SUCCESS)).thenReturn(5L);

        // when
        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        // then
        assertThat(result).isEmpty();
    }
}
