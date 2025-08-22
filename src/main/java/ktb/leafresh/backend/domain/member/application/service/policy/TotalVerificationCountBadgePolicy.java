package ktb.leafresh.backend.domain.member.application.service.policy;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.service.policy.BadgeGrantPolicy;
import ktb.leafresh.backend.domain.member.infrastructure.repository.BadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TotalVerificationCountBadgePolicy implements BadgeGrantPolicy {

  private final GroupChallengeVerificationRepository groupVerificationRepository;
  private final PersonalChallengeVerificationRepository personalVerificationRepository;
  private final BadgeRepository badgeRepository;
  private final MemberBadgeRepository memberBadgeRepository;

  private static final Map<Integer, String> countToBadge =
      Map.of(
          10, "첫 발자국",
          30, "실천 중급자",
          50, "지속가능 파이터",
          100, "그린 마스터");

  @Override
  public List<Badge> evaluateAndGetNewBadges(Member member) {
    List<Badge> newBadges = new ArrayList<>();

    long total =
        groupVerificationRepository.countByMemberIdAndStatus(
                member.getId(), ChallengeStatus.SUCCESS)
            + personalVerificationRepository.countTotalByMemberIdAndStatus(
                member.getId(), ChallengeStatus.SUCCESS);

    for (Map.Entry<Integer, String> entry : countToBadge.entrySet()) {
      if (total >= entry.getKey()) {
        badgeRepository
            .findByName(entry.getValue())
            .ifPresent(
                badge -> {
                  if (!memberBadgeRepository.existsByMemberAndBadge(member, badge)) {
                    newBadges.add(badge);
                  }
                });
      }
    }

    return newBadges;
  }
}
