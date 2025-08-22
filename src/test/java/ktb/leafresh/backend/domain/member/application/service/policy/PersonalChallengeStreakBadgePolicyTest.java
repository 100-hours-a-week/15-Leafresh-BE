package ktb.leafresh.backend.domain.member.application.service.policy;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.BadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PersonalChallengeStreakBadgePolicyTest {

  @Mock private PersonalChallengeVerificationRepository personalVerificationRepository;

  @Mock private BadgeRepository badgeRepository;

  @Mock private MemberBadgeRepository memberBadgeRepository;

  @InjectMocks private PersonalChallengeStreakBadgePolicy badgePolicy;

  private Member member;

  @BeforeEach
  void setUp() {
    member = MemberFixture.of();
  }

  @Test
  @DisplayName("연속 인증이 30일 이상이면 4개 뱃지 모두 지급")
  void evaluateAndGetNewBadges_with30Streak_grantsAllBadges() {
    // given
    int streak = 30;

    given(personalVerificationRepository.countConsecutiveSuccessDays(member.getId()))
        .willReturn(streak);

    Badge badge3 = BadgeFixture.of("새싹 실천러");
    Badge badge7 = BadgeFixture.of("일주일의 습관");
    Badge badge14 = BadgeFixture.of("반달 에코러");
    Badge badge30 = BadgeFixture.of("한 달 챌린지 완주자");

    given(badgeRepository.findByName("새싹 실천러")).willReturn(Optional.of(badge3));
    given(badgeRepository.findByName("일주일의 습관")).willReturn(Optional.of(badge7));
    given(badgeRepository.findByName("반달 에코러")).willReturn(Optional.of(badge14));
    given(badgeRepository.findByName("한 달 챌린지 완주자")).willReturn(Optional.of(badge30));

    given(memberBadgeRepository.existsByMemberAndBadge(member, badge3)).willReturn(false);
    given(memberBadgeRepository.existsByMemberAndBadge(member, badge7)).willReturn(false);
    given(memberBadgeRepository.existsByMemberAndBadge(member, badge14)).willReturn(false);
    given(memberBadgeRepository.existsByMemberAndBadge(member, badge30)).willReturn(false);

    // when
    List<Badge> result = badgePolicy.evaluateAndGetNewBadges(member);

    // then
    assertThat(result).containsExactlyInAnyOrder(badge3, badge7, badge14, badge30);
  }

  @Test
  @DisplayName("연속 인증이 7일이면 3개 뱃지 지급 (3, 7)")
  void evaluateAndGetNewBadges_with7Streak_grantsThreeBadges() {
    // given
    int streak = 7;

    given(personalVerificationRepository.countConsecutiveSuccessDays(member.getId()))
        .willReturn(streak);

    Badge badge3 = BadgeFixture.of("새싹 실천러");
    Badge badge7 = BadgeFixture.of("일주일의 습관");

    given(badgeRepository.findByName("새싹 실천러")).willReturn(Optional.of(badge3));
    given(badgeRepository.findByName("일주일의 습관")).willReturn(Optional.of(badge7));

    given(memberBadgeRepository.existsByMemberAndBadge(member, badge3)).willReturn(false);
    given(memberBadgeRepository.existsByMemberAndBadge(member, badge7)).willReturn(false);

    // when
    List<Badge> result = badgePolicy.evaluateAndGetNewBadges(member);

    // then
    assertThat(result).containsExactlyInAnyOrder(badge3, badge7);
  }

  @Test
  @DisplayName("연속 인증이 3일인데 이미 뱃지를 보유한 경우 부여하지 않음")
  void evaluateAndGetNewBadges_alreadyHasBadge_doesNotGrant() {
    // given
    int streak = 3;

    Badge badge = BadgeFixture.of("새싹 실천러");

    given(personalVerificationRepository.countConsecutiveSuccessDays(member.getId()))
        .willReturn(streak);
    given(badgeRepository.findByName("새싹 실천러")).willReturn(Optional.of(badge));
    given(memberBadgeRepository.existsByMemberAndBadge(member, badge)).willReturn(true);

    // when
    List<Badge> result = badgePolicy.evaluateAndGetNewBadges(member);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("연속 인증이 2일이면 뱃지 지급되지 않음")
  void evaluateAndGetNewBadges_with2Streak_grantsNothing() {
    // given
    given(personalVerificationRepository.countConsecutiveSuccessDays(member.getId())).willReturn(2);

    // when
    List<Badge> result = badgePolicy.evaluateAndGetNewBadges(member);

    // then
    assertThat(result).isEmpty();
    then(badgeRepository).shouldHaveNoInteractions();
    then(memberBadgeRepository).shouldHaveNoInteractions();
  }
}
