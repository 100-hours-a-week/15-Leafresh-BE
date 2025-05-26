package ktb.leafresh.backend.domain.member.application.service.policy;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.enums.BadgeType;
import ktb.leafresh.backend.domain.member.infrastructure.repository.BadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
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

class GroupChallengeCategoryBadgePolicyTest {

    private GroupChallengeVerificationRepository groupVerificationRepository;
    private BadgeRepository badgeRepository;
    private MemberBadgeRepository memberBadgeRepository;
    private GroupChallengeCategoryRepository groupChallengeCategoryRepository;
    private GroupChallengeCategoryBadgePolicy policy;

    @BeforeEach
    void setUp() {
        groupVerificationRepository = mock(GroupChallengeVerificationRepository.class);
        badgeRepository = mock(BadgeRepository.class);
        memberBadgeRepository = mock(MemberBadgeRepository.class);
        groupChallengeCategoryRepository = mock(GroupChallengeCategoryRepository.class);
        policy = new GroupChallengeCategoryBadgePolicy(
                groupVerificationRepository,
                badgeRepository,
                memberBadgeRepository,
                groupChallengeCategoryRepository
        );
    }

    @Test
    @DisplayName("단체 챌린지 카테고리 인증 3회 이상 성공 시 뱃지가 지급된다")
    void evaluateAndGetNewBadges_CategorySuccessOver3_ThenGrantBadge() {
        Member member = MemberFixture.of();
        String categoryName = "제로웨이스트";
        String badgeName = "제로 히어로";
        GroupChallengeCategory categoryEntity = mock(GroupChallengeCategory.class);
        Badge badge = BadgeFixture.of(1L, badgeName, BadgeType.GROUP);

        when(groupChallengeCategoryRepository.findByName(categoryName)).thenReturn(Optional.of(categoryEntity));
        when(groupVerificationRepository.countDistinctChallengesByMemberIdAndCategoryAndStatus(
                member.getId(), categoryEntity, ChallengeStatus.SUCCESS)).thenReturn(3L);
        when(badgeRepository.findByName(badgeName)).thenReturn(Optional.of(badge));
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge)).thenReturn(false);

        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        assertThat(result).containsExactly(badge);
    }

    @Test
    @DisplayName("이미 해당 카테고리 뱃지를 보유 중이라면 새로 지급하지 않는다")
    void evaluateAndGetNewBadges_AlreadyHasBadge_ThenNoNewBadge() {
        Member member = MemberFixture.of();
        String categoryName = "제로웨이스트";
        String badgeName = "제로 히어로";
        GroupChallengeCategory categoryEntity = mock(GroupChallengeCategory.class);
        Badge badge = BadgeFixture.of(2L, badgeName, BadgeType.GROUP);

        when(groupChallengeCategoryRepository.findByName(categoryName)).thenReturn(Optional.of(categoryEntity));
        when(groupVerificationRepository.countDistinctChallengesByMemberIdAndCategoryAndStatus(
                member.getId(), categoryEntity, ChallengeStatus.SUCCESS)).thenReturn(4L);
        when(badgeRepository.findByName(badgeName)).thenReturn(Optional.of(badge));
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge)).thenReturn(true);

        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("해당 카테고리 인증 성공이 부족하면 뱃지를 지급하지 않는다")
    void evaluateAndGetNewBadges_NotEnoughSuccess_ThenNoBadge() {
        Member member = MemberFixture.of();
        String categoryName = "제로웨이스트";
        String badgeName = "제로 히어로";
        GroupChallengeCategory categoryEntity = mock(GroupChallengeCategory.class);

        when(groupChallengeCategoryRepository.findByName(categoryName)).thenReturn(Optional.of(categoryEntity));
        when(groupVerificationRepository.countDistinctChallengesByMemberIdAndCategoryAndStatus(
                member.getId(), categoryEntity, ChallengeStatus.SUCCESS)).thenReturn(2L);

        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        assertThat(result).isEmpty();
    }
}
