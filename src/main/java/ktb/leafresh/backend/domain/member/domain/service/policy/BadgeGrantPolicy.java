package ktb.leafresh.backend.domain.member.domain.service.policy;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;

import java.util.List;

public interface BadgeGrantPolicy {
  List<Badge> evaluateAndGetNewBadges(Member member);
}
