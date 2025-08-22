package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.MemberBadge;
import ktb.leafresh.backend.domain.member.domain.service.policy.BadgeGrantPolicy;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BadgeGrantManager {

  private final List<BadgeGrantPolicy> policies;
  private final MemberBadgeRepository memberBadgeRepository;

  @Transactional
  public void evaluateAllAndGrant(Member member) {
    for (BadgeGrantPolicy policy : policies) {
      List<Badge> newBadges = policy.evaluateAndGetNewBadges(member);
      boolean grantedAny = false;
      for (Badge badge : newBadges) {
        if (!memberBadgeRepository.existsByMemberAndBadge(member, badge)) {
          memberBadgeRepository.save(MemberBadge.of(member, badge));
          log.info(
              "[뱃지 지급] memberId={}, badgeName={}, policy={}",
              member.getId(),
              badge.getName(),
              policy.getClass().getSimpleName());
          grantedAny = true;
        }
      }
      if (!grantedAny) {
        log.debug(
            "[뱃지 없음] memberId={}, policy={}", member.getId(), policy.getClass().getSimpleName());
      }
    }
  }
}
