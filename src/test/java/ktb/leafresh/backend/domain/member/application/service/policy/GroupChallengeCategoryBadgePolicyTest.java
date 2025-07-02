package ktb.leafresh.backend.domain.member.application.service.policy;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.BadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.support.fixture.BadgeFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class GroupChallengeCategoryBadgePolicyTest {

    @Mock
    private GroupChallengeVerificationRepository groupVerificationRepository;

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private MemberBadgeRepository memberBadgeRepository;

    @Mock
    private GroupChallengeCategoryRepository categoryRepository;

    @InjectMocks
    private GroupChallengeCategoryBadgePolicy badgePolicy;

    private Member member;

    @BeforeEach
    void setUp() {
        member = MemberFixture.of();

        // 모든 카테고리명에 대한 기본 Stubbing (없다고 가정)
        lenient().when(categoryRepository.findByName(anyString()))
                .thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("3개 이상 성공 인증 시 카테고리 뱃지 부여")
    void evaluateAndGetNewBadges_withThreeSuccesses_grantsBadge() {
        // given
        String categoryName = "플로깅";
        String badgeName = "플로깅 파이터";

        GroupChallengeCategory category = GroupChallengeCategory.builder()
                .name(categoryName)
                .build();
        Badge badge = BadgeFixture.of(badgeName);

        given(categoryRepository.findByName(categoryName)).willReturn(Optional.of(category));
        given(groupVerificationRepository.countDistinctChallengesByMemberIdAndCategoryAndStatus(
                member.getId(), category, SUCCESS)).willReturn(3L);
        given(badgeRepository.findByName(badgeName)).willReturn(Optional.of(badge));
        given(memberBadgeRepository.existsByMemberAndBadge(member, badge)).willReturn(false);

        // when
        List<Badge> result = badgePolicy.evaluateAndGetNewBadges(member);

        // then
        assertThat(result).containsExactly(badge);
    }

    @Test
    @DisplayName("뱃지를 이미 보유한 경우 부여하지 않음")
    void evaluateAndGetNewBadges_alreadyHasBadge_doesNotGrant() {
        // given
        String categoryName = "제로웨이스트";
        String badgeName = "제로 히어로";

        GroupChallengeCategory category = GroupChallengeCategory.builder()
                .name(categoryName)
                .build();
        Badge badge = BadgeFixture.of(badgeName);

        given(categoryRepository.findByName(categoryName)).willReturn(Optional.of(category));
        given(groupVerificationRepository.countDistinctChallengesByMemberIdAndCategoryAndStatus(
                member.getId(), category, SUCCESS)).willReturn(3L);
        given(badgeRepository.findByName(badgeName)).willReturn(Optional.of(badge));
        given(memberBadgeRepository.existsByMemberAndBadge(member, badge)).willReturn(true);

        // when
        List<Badge> result = badgePolicy.evaluateAndGetNewBadges(member);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("성공 인증이 부족하면 뱃지 부여하지 않음")
    void evaluateAndGetNewBadges_insufficientSuccesses_doesNotGrant() {
        // given
        String categoryName = "에너지 절약";
        String badgeName = "절전 마스터";

        GroupChallengeCategory category = GroupChallengeCategory.builder()
                .name(categoryName)
                .build();

        given(categoryRepository.findByName(categoryName)).willReturn(Optional.of(category));
        given(groupVerificationRepository.countDistinctChallengesByMemberIdAndCategoryAndStatus(
                member.getId(), category, SUCCESS)).willReturn(2L);

        // when
        List<Badge> result = badgePolicy.evaluateAndGetNewBadges(member);

        // then
        assertThat(result).isEmpty();
        then(badgeRepository).should(never()).findByName(any());
    }

    @Test
    @DisplayName("카테고리 엔티티가 존재하지 않으면 스킵")
    void evaluateAndGetNewBadges_categoryNotFound_skips() {
        // given
        String categoryName = "비건";

        // 이미 setUp에서 기본적으로 Optional.empty()로 설정됨

        // when
        List<Badge> result = badgePolicy.evaluateAndGetNewBadges(member);

        // then
        assertThat(result).isEmpty();
    }
}
