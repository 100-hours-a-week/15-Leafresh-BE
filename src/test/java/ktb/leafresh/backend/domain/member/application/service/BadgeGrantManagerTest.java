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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class BadgeGrantManagerTest {

  @Mock private BadgeGrantPolicy policy1;
  @Mock private BadgeGrantPolicy policy2;
  @Mock private MemberBadgeRepository memberBadgeRepository;

  @InjectMocks private BadgeGrantManager badgeGrantManager;

  private Member member;
  private Badge badge1;
  private Badge badge2;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    badgeGrantManager = new BadgeGrantManager(List.of(policy1, policy2), memberBadgeRepository);
    member = MemberFixture.of();
    badge1 = BadgeFixture.of("지속가능 전도사");
    badge2 = BadgeFixture.of("에코 슈퍼루키");
  }

  @Test
  @DisplayName("정책별 새 뱃지가 존재할 경우 저장됨")
  void evaluateAllAndGrant_success() {
    // given
    given(policy1.evaluateAndGetNewBadges(member)).willReturn(List.of(badge1));
    given(policy2.evaluateAndGetNewBadges(member)).willReturn(List.of(badge2));
    given(memberBadgeRepository.existsByMemberAndBadge(member, badge1)).willReturn(false);
    given(memberBadgeRepository.existsByMemberAndBadge(member, badge2)).willReturn(false);

    // when & then
    assertThatCode(() -> badgeGrantManager.evaluateAllAndGrant(member)).doesNotThrowAnyException();
    verify(memberBadgeRepository, times(2)).save(any(MemberBadge.class));
  }

  @Test
  @DisplayName("이미 보유한 뱃지는 저장되지 않음")
  void evaluateAllAndGrant_alreadyOwnedBadge() {
    // given
    given(policy1.evaluateAndGetNewBadges(member)).willReturn(List.of(badge1));
    given(memberBadgeRepository.existsByMemberAndBadge(member, badge1)).willReturn(true);

    // when
    badgeGrantManager.evaluateAllAndGrant(member);

    // then
    verify(memberBadgeRepository, never()).save(any());
  }

  @Test
  @DisplayName("뱃지가 아예 없는 경우에도 정상 처리")
  void evaluateAllAndGrant_noBadges() {
    // given
    given(policy1.evaluateAndGetNewBadges(member)).willReturn(List.of());
    given(policy2.evaluateAndGetNewBadges(member)).willReturn(List.of());

    // when
    badgeGrantManager.evaluateAllAndGrant(member);

    // then
    verify(memberBadgeRepository, never()).save(any());
  }
}
