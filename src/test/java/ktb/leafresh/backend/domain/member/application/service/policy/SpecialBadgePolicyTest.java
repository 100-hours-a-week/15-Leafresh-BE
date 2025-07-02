package ktb.leafresh.backend.domain.member.application.service.policy;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.enums.GroupChallengeCategoryName;
import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.BadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
import ktb.leafresh.backend.support.fixture.BadgeFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static ktb.leafresh.backend.domain.challenge.group.domain.entity.enums.GroupChallengeCategoryName.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

class SpecialBadgePolicyTest {

    @Mock
    private GroupChallengeVerificationRepository groupVerificationRepository;

    @Mock
    private PersonalChallengeVerificationRepository personalVerificationRepository;

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private MemberBadgeRepository memberBadgeRepository;

    @Mock
    private GroupChallengeCategoryRepository groupChallengeCategoryRepository;

    @InjectMocks private SpecialBadgePolicy specialBadgePolicy;

    private Member member;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        member = MemberFixture.of();
    }

    @Test
    @DisplayName("모든 단체 챌린지 카테고리 인증 성공 시 '지속가능 전도사' 뱃지 지급")
    void evaluateAllGroupCategoriesCleared_returnsBadge() {
        // given
        for (GroupChallengeCategoryName name : GroupChallengeCategoryName.values()) {
            if (name == GroupChallengeCategoryName.ETC) continue;
            GroupChallengeCategory category = GroupChallengeCategory.builder().name(name.name()).build();

            given(groupChallengeCategoryRepository.findByName(name.name())).willReturn(Optional.of(category));
            given(groupVerificationRepository.existsByMemberIdAndCategoryAndStatus(member.getId(), category, ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus.SUCCESS))
                    .willReturn(true);
        }

        Badge badge = BadgeFixture.of("지속가능 전도사");
        given(badgeRepository.findByName("지속가능 전도사")).willReturn(Optional.of(badge));
        given(memberBadgeRepository.existsByMemberAndBadge(member, badge)).willReturn(false);

        // when
        List<Badge> newBadges = specialBadgePolicy.evaluateAndGetNewBadges(member);

        // then
        assertThat(newBadges).containsExactly(badge);
    }

    @Test
    @DisplayName("30일 연속 개인 챌린지 인증 성공 시 '에코 슈퍼루키' 뱃지 지급")
    void evaluatePersonalStreak_returnsBadge() {
        // given
        given(personalVerificationRepository.countConsecutiveSuccessDays(member.getId())).willReturn(30);

        Badge badge = BadgeFixture.of("에코 슈퍼루키");
        given(badgeRepository.findByName("에코 슈퍼루키")).willReturn(Optional.of(badge));
        given(memberBadgeRepository.existsByMemberAndBadge(member, badge)).willReturn(false);

        // when
        List<Badge> newBadges = specialBadgePolicy.evaluateAndGetNewBadges(member);

        // then
        assertThat(newBadges).containsExactly(badge);
    }

    @Test
    @DisplayName("개인 챌린지 모든 제목 인증 성공 시 '도전 전부러' 뱃지 지급")
    void evaluateAllPersonalChallengesCleared_returnsBadge() {
        // given
        List<String> challengeTitles = List.of("물 아껴쓰기", "대중교통 이용하기");
        given(personalVerificationRepository.findAllPersonalChallengeTitles()).willReturn(challengeTitles);

        for (String title : challengeTitles) {
            given(personalVerificationRepository.existsByMemberIdAndPersonalChallengeTitleAndStatus(
                    member.getId(), title, ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus.SUCCESS)).willReturn(true);
        }

        Badge badge = BadgeFixture.of("도전 전부러");
        given(badgeRepository.findByName("도전 전부러")).willReturn(Optional.of(badge));
        given(memberBadgeRepository.existsByMemberAndBadge(member, badge)).willReturn(false);

        // when
        List<Badge> newBadges = specialBadgePolicy.evaluateAndGetNewBadges(member);

        // then
        assertThat(newBadges).containsExactly(badge);
    }

    @Test
    @DisplayName("카테고리별 10회 인증 성공 시 '카테고리 마스터' 뱃지 지급")
    void evaluateCategoryMaster_returnsBadge() {
        // given
        GroupChallengeCategoryName categoryName = PLOGGING;
        GroupChallengeCategory category = GroupChallengeCategory.builder().name(categoryName.name()).build();
        given(groupChallengeCategoryRepository.findByName(categoryName.name())).willReturn(Optional.of(category));
        given(groupVerificationRepository.countByMemberIdAndCategoryAndStatus(
                member.getId(), category, ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus.SUCCESS)).willReturn(10L);

        Badge badge = BadgeFixture.of("플로깅 마스터");
        given(badgeRepository.findByName("플로깅 마스터")).willReturn(Optional.of(badge));
        given(memberBadgeRepository.existsByMemberAndBadge(member, badge)).willReturn(false);

        // when
        List<Badge> newBadges = specialBadgePolicy.evaluateAndGetNewBadges(member);

        // then
        assertThat(newBadges).containsExactly(badge);
    }

    @Test
    @DisplayName("이미 보유한 뱃지는 지급되지 않음")
    void alreadyHasBadge_shouldNotDuplicate() {
        // given
        given(personalVerificationRepository.countConsecutiveSuccessDays(member.getId())).willReturn(30);

        Badge badge = BadgeFixture.of("에코 슈퍼루키");
        given(badgeRepository.findByName("에코 슈퍼루키")).willReturn(Optional.of(badge));
        given(memberBadgeRepository.existsByMemberAndBadge(member, badge)).willReturn(true);

        // when
        List<Badge> newBadges = specialBadgePolicy.evaluateAndGetNewBadges(member);

        // then
        assertThat(newBadges).isEmpty();
    }
}
