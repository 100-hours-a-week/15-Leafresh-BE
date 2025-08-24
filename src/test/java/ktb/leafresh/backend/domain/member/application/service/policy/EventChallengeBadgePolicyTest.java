package ktb.leafresh.backend.domain.member.application.service.policy;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.BadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
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
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EventChallengeBadgePolicyTest {

  @Mock private GroupChallengeVerificationRepository groupVerificationRepository;

  @Mock private BadgeRepository badgeRepository;

  @Mock private MemberBadgeRepository memberBadgeRepository;

  @InjectMocks private EventChallengeBadgePolicy badgePolicy;

  private Member member;
  private static final String EVENT_TITLE = "세계 물의 날";
  private static final String BADGE_NAME = "물수호대";

  @BeforeEach
  void setUp() {
    member = MemberFixture.of(); // ID 없음, 핵심 필드만 세팅
  }

  @Test
  @DisplayName("이벤트 인증 3회 성공 시 해당 뱃지를 지급한다")
  void evaluateAndGetNewBadges_withValidEvent_shouldReturnBadge() {
    // given
    Badge expectedBadge = BadgeFixture.of(BADGE_NAME);

    given(groupVerificationRepository.findDistinctEventTitlesWithEventFlagTrue())
        .willReturn(List.of(EVENT_TITLE));

    given(
            groupVerificationRepository.countByMemberIdAndEventTitleAndStatus(
                member.getId(), EVENT_TITLE, ChallengeStatus.SUCCESS))
        .willReturn(3L); // 조건 만족

    given(badgeRepository.findByName(BADGE_NAME)).willReturn(Optional.of(expectedBadge));

    given(memberBadgeRepository.existsByMemberAndBadge(member, expectedBadge))
        .willReturn(false); // 아직 보유 안함

    // when
    List<Badge> result = badgePolicy.evaluateAndGetNewBadges(member);

    // then
    assertThat(result).containsExactly(expectedBadge);
  }

  @Test
  @DisplayName("이벤트 인증이 3회 미만이면 뱃지를 지급하지 않는다")
  void evaluateAndGetNewBadges_withLessThan3_shouldReturnEmpty() {
    // given
    given(groupVerificationRepository.findDistinctEventTitlesWithEventFlagTrue())
        .willReturn(List.of(EVENT_TITLE));

    given(
            groupVerificationRepository.countByMemberIdAndEventTitleAndStatus(
                member.getId(), EVENT_TITLE, ChallengeStatus.SUCCESS))
        .willReturn(2L); // 조건 미달

    // when
    List<Badge> result = badgePolicy.evaluateAndGetNewBadges(member);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("이미 보유한 뱃지는 다시 지급하지 않는다")
  void evaluateAndGetNewBadges_whenAlreadyOwned_shouldReturnEmpty() {
    // given
    Badge badge = BadgeFixture.of(BADGE_NAME);

    given(groupVerificationRepository.findDistinctEventTitlesWithEventFlagTrue())
        .willReturn(List.of(EVENT_TITLE));

    given(
            groupVerificationRepository.countByMemberIdAndEventTitleAndStatus(
                member.getId(), EVENT_TITLE, ChallengeStatus.SUCCESS))
        .willReturn(3L); // 조건 만족

    given(badgeRepository.findByName(BADGE_NAME)).willReturn(Optional.of(badge));

    given(memberBadgeRepository.existsByMemberAndBadge(member, badge)).willReturn(true); // 이미 보유함

    // when
    List<Badge> result = badgePolicy.evaluateAndGetNewBadges(member);

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("이벤트명이 매핑되지 않은 경우 로그만 남기고 무시한다")
  void evaluateAndGetNewBadges_whenEventTitleNotMapped_shouldIgnore() {
    // given
    String unknownEventTitle = "알 수 없는 이벤트";

    given(groupVerificationRepository.findDistinctEventTitlesWithEventFlagTrue())
        .willReturn(List.of(unknownEventTitle));

    given(
            groupVerificationRepository.countByMemberIdAndEventTitleAndStatus(
                member.getId(), unknownEventTitle, ChallengeStatus.SUCCESS))
        .willReturn(3L); // 조건은 만족하지만 매핑 없음

    // when
    List<Badge> result = badgePolicy.evaluateAndGetNewBadges(member);

    // then
    assertThat(result).isEmpty();
  }
}
