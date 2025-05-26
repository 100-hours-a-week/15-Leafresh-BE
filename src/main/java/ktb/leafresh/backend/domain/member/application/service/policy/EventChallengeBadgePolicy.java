package ktb.leafresh.backend.domain.member.application.service.policy;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.service.policy.BadgeGrantPolicy;
import ktb.leafresh.backend.domain.member.infrastructure.repository.BadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventChallengeBadgePolicy implements BadgeGrantPolicy {

    private final GroupChallengeVerificationRepository groupVerificationRepository;
    private final BadgeRepository badgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;

    // 이벤트명 → 뱃지명 매핑
    private static final Map<String, String> eventToBadgeName = Map.ofEntries(
            Map.entry("세계 습지의 날", "습지 전도사"),
            Map.entry("고래의 날", "바다 지킴이"),
            Map.entry("세계 물의 날", "물수호대"),
            Map.entry("식목일", "나무 한 그루의 기적"),
            Map.entry("지구의 날", "지구에게 쓴 편지"),
            Map.entry("세계 퇴비 주간", "퇴비 마스터"),
            Map.entry("공정무역의 날", "착한 소비러"),
            Map.entry("바다의 날", "파도 위 발자국"),
            Map.entry("환경의 날", "환경 루틴러"),
            Map.entry("사막화와 가뭄 방지의 날", "양치컵 히어로"),
            Map.entry("세계 호랑이의 날", "호랑이 친구"),
            Map.entry("에너지의 날", "OFF 마스터"),
            Map.entry("자원순환의 날", "자원순환 챔피언"),
            Map.entry("세계 차 없는 날", "무탄소 여행자"),
            Map.entry("세계 자연재해 감소의 날", "기후 기록자"),
            Map.entry("세계 식량의 날", "비건 한 끼 도전자"),
            Map.entry("농민의 날", "도시 농부")
    );

    @Override
    public List<Badge> evaluateAndGetNewBadges(Member member) {
        List<Badge> newBadges = new ArrayList<>();

        List<String> eventTitles = groupVerificationRepository.findDistinctEventTitlesWithEventFlagTrue();
        for (String eventTitle : eventTitles) {
            long count = groupVerificationRepository.countByMemberIdAndEventTitleAndStatus(
                    member.getId(), eventTitle, ChallengeStatus.SUCCESS);

            if (count >= 3) {
                String badgeName = eventToBadgeName.get(eventTitle);
                if (badgeName != null) {
                    badgeRepository.findByName(badgeName).ifPresent(badge -> {
                        if (!memberBadgeRepository.existsByMemberAndBadge(member, badge)) {
                            newBadges.add(badge);
                        }
                    });
                } else {
                    // 알 수 없는 이벤트명 대응 (로그만)
                    log.warn("[이벤트 뱃지] 매핑되지 않은 이벤트명: {}", eventTitle);
                }
            }
        }

        return newBadges;
    }
}
