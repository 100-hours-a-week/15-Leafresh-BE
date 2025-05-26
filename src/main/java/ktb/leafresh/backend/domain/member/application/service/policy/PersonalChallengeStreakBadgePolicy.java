package ktb.leafresh.backend.domain.member.application.service.policy;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.service.policy.BadgeGrantPolicy;
import ktb.leafresh.backend.domain.member.infrastructure.repository.BadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PersonalChallengeStreakBadgePolicy implements BadgeGrantPolicy {

    private final PersonalChallengeVerificationRepository personalVerificationRepository;
    private final BadgeRepository badgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;

    // 연속 인증 기준 → 뱃지명
    private static final Map<Integer, String> streakToBadgeName = Map.of(
            3, "새싹 실천러",
            7, "일주일의 습관",
            14, "반달 에코러",
            30, "한 달 챌린지 완주자"
    );

    @Override
    public List<Badge> evaluateAndGetNewBadges(Member member) {
        List<Badge> newBadges = new ArrayList<>();

        int streakDays = personalVerificationRepository.countConsecutiveSuccessDays(member.getId());

        for (Map.Entry<Integer, String> entry : streakToBadgeName.entrySet()) {
            if (streakDays >= entry.getKey()) {
                badgeRepository.findByName(entry.getValue()).ifPresent(badge -> {
                    if (!memberBadgeRepository.existsByMemberAndBadge(member, badge)) {
                        newBadges.add(badge);
                    }
                });
            }
        }

        return newBadges;
    }
}
