package ktb.leafresh.backend.domain.member.application.service.policy;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.service.policy.BadgeGrantPolicy;
import ktb.leafresh.backend.domain.member.infrastructure.repository.BadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class GroupChallengeCategoryBadgePolicy implements BadgeGrantPolicy {

    private final GroupChallengeVerificationRepository groupVerificationRepository;
    private final BadgeRepository badgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;
    private final GroupChallengeCategoryRepository groupChallengeCategoryRepository;

    // 단체 챌린지 카테고리명 → 뱃지명 매핑
    private static final Map<String, String> categoryToBadge = Map.of(
            "제로웨이스트", "제로 히어로",
            "플로깅", "플로깅 파이터",
            "탄소 발자국", "발자국 줄이기 고수",
            "에너지 절약", "절전 마스터",
            "업사이클", "새활용 장인",
            "문화 공유", "녹색 지식인",
            "디지털 탄소", "디지털 디톡서",
            "비건", "비건 챌린저"
    );

    @Override
    public List<Badge> evaluateAndGetNewBadges(Member member) {
        List<Badge> newBadges = new ArrayList<>();

        for (Map.Entry<String, String> entry : categoryToBadge.entrySet()) {
            String categoryName = entry.getKey();
            String badgeName = entry.getValue();

            // 문자열 → GroupChallengeCategory entity 조회
            Optional<GroupChallengeCategory> optionalCategory =
                    groupChallengeCategoryRepository.findByName(categoryName);

            if (optionalCategory.isEmpty()) {
                continue; // DB에 없는 카테고리는 스킵
            }

            GroupChallengeCategory category = optionalCategory.get();

            long distinctSuccesses = groupVerificationRepository.countDistinctChallengesByMemberIdAndCategoryAndStatus(
                    member.getId(), category, ChallengeStatus.SUCCESS);

            if (distinctSuccesses >= 3) {
                badgeRepository.findByName(badgeName).ifPresent(badge -> {
                    if (!memberBadgeRepository.existsByMemberAndBadge(member, badge)) {
                        newBadges.add(badge);
                    }
                });
            }
        }

        return newBadges;
    }
}
