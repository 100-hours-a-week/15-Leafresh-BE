//package ktb.leafresh.backend.domain.member.application.service.policy;
//
//import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
//import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
//import ktb.leafresh.backend.domain.member.domain.entity.Badge;
//import ktb.leafresh.backend.domain.member.domain.entity.Member;
//import ktb.leafresh.backend.domain.member.domain.entity.enums.BadgeType;
//import ktb.leafresh.backend.domain.member.infrastructure.repository.BadgeRepository;
//import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
//import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
//import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
//import ktb.leafresh.backend.support.fixture.BadgeFixture;
//import ktb.leafresh.backend.support.fixture.MemberFixture;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("GroupChallengeCategoryBadgePolicy 테스트")
//class GroupChallengeCategoryBadgePolicyTest {
//
//    @Mock
//    private GroupChallengeVerificationRepository groupVerificationRepository;
//
//    @Mock
//    private BadgeRepository badgeRepository;
//
//    @Mock
//    private MemberBadgeRepository memberBadgeRepository;
//
//    @Mock
//    private GroupChallengeCategoryRepository groupChallengeCategoryRepository;
//
//    @InjectMocks
//    private GroupChallengeCategoryBadgePolicy policy;
//
//    private final String categoryName = "제로웨이스트";
//    private final String badgeName = "제로 히어로";
//
//    private Member member;
//    private GroupChallengeCategory category;
//    private Badge badge;
//
//    @BeforeEach
//    void setUp() {
//        member = MemberFixture.of();
//        category = mock(GroupChallengeCategory.class);
//        badge = BadgeFixture.of(badgeName, BadgeType.GROUP);
//    }
//
//    @Test
//    @DisplayName("단체 챌린지 카테고리 인증 3회 이상 성공 시 뱃지가 지급된다")
//    void evaluateAndGetNewBadges_CategorySuccessOver3_ThenGrantBadge() {
//        // given
//        when(groupChallengeCategoryRepository.findByName(categoryName)).thenReturn(Optional.of(category));
//        when(groupVerificationRepository.countDistinctChallengesByMemberIdAndCategoryAndStatus(
//                member.getId(), category, ChallengeStatus.SUCCESS)).thenReturn(3L);
//        when(badgeRepository.findByName(badgeName)).thenReturn(Optional.of(badge));
//        when(memberBadgeRepository.existsByMemberAndBadge(member, badge)).thenReturn(false);
//
//        // when
//        List<Badge> result = policy.evaluateAndGetNewBadges(member);
//
//        // then
//        assertThat(result).containsExactly(badge);
//    }
//
//    @Test
//    @DisplayName("이미 해당 카테고리 뱃지를 보유 중이라면 새로 지급하지 않는다")
//    void evaluateAndGetNewBadges_AlreadyHasBadge_ThenNoNewBadge() {
//        // given
//        when(groupChallengeCategoryRepository.findByName(categoryName)).thenReturn(Optional.of(category));
//        when(groupVerificationRepository.countDistinctChallengesByMemberIdAndCategoryAndStatus(
//                member.getId(), category, ChallengeStatus.SUCCESS)).thenReturn(4L);
//        when(badgeRepository.findByName(badgeName)).thenReturn(Optional.of(badge));
//        when(memberBadgeRepository.existsByMemberAndBadge(member, badge)).thenReturn(true);
//
//        // when
//        List<Badge> result = policy.evaluateAndGetNewBadges(member);
//
//        // then
//        assertThat(result).isEmpty();
//    }
//
//    @Test
//    @DisplayName("해당 카테고리 인증 성공이 부족하면 뱃지를 지급하지 않는다")
//    void evaluateAndGetNewBadges_NotEnoughSuccess_ThenNoBadge() {
//        // given
//        when(groupChallengeCategoryRepository.findByName(categoryName)).thenReturn(Optional.of(category));
//        when(groupVerificationRepository.countDistinctChallengesByMemberIdAndCategoryAndStatus(
//                member.getId(), category, ChallengeStatus.SUCCESS)).thenReturn(2L);
//
//        // when
//        List<Badge> result = policy.evaluateAndGetNewBadges(member);
//
//        // then
//        assertThat(result).isEmpty();
//    }
//}
