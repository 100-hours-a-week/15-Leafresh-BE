package ktb.leafresh.backend.domain.member.application.service.policy;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.enums.GroupChallengeCategoryName;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
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

import java.util.*;

@Component
@RequiredArgsConstructor
public class SpecialBadgePolicy implements BadgeGrantPolicy {

  private final GroupChallengeVerificationRepository groupVerificationRepository;
  private final PersonalChallengeVerificationRepository personalVerificationRepository;
  private final BadgeRepository badgeRepository;
  private final MemberBadgeRepository memberBadgeRepository;
  private final GroupChallengeCategoryRepository groupChallengeCategoryRepository;

  private static final Map<String, String> categoryToBadge =
      Map.of(
          "제로웨이스트", "제로웨이스트",
          "플로깅", "플로깅",
          "탄소 발자국", "탄소 발자국",
          "에너지 절약", "에너지 절약",
          "업사이클", "업사이클",
          "문화 공유", "문화 공유",
          "디지털 탄소", "디지털 탄소",
          "비건", "비건");

  @Override
  public List<Badge> evaluateAndGetNewBadges(Member member) {
    List<Badge> newBadges = new ArrayList<>();

    // 1. 모든 단체 카테고리 1회 이상 인증 성공 → 지속가능 전도사
    List<GroupChallengeCategoryName> requiredCategories =
        Arrays.stream(GroupChallengeCategoryName.values())
            .filter(category -> category != GroupChallengeCategoryName.ETC)
            .toList();

    boolean allGroupCategoriesCleared =
        requiredCategories.stream()
            .allMatch(
                enumName ->
                    groupChallengeCategoryRepository
                        .findByName(enumName.name())
                        .map(
                            categoryEntity ->
                                groupVerificationRepository.existsByMemberIdAndCategoryAndStatus(
                                    member.getId(), categoryEntity, ChallengeStatus.SUCCESS))
                        .orElse(false));
    if (allGroupCategoriesCleared) {
      addIfNotExists(member, "지속가능 전도사", newBadges);
    }

    // 2. 모든 개인 챌린지 유형 1회 이상 인증 성공 → 도전 전부러
    List<String> allPersonalChallengeTitles =
        personalVerificationRepository.findAllPersonalChallengeTitles();
    boolean allPersonalChallengesCleared =
        allPersonalChallengeTitles.stream()
            .allMatch(
                title ->
                    personalVerificationRepository
                        .existsByMemberIdAndPersonalChallengeTitleAndStatus(
                            member.getId(), title, ChallengeStatus.SUCCESS));
    if (allPersonalChallengesCleared) {
      addIfNotExists(member, "도전 전부러", newBadges);
    }

    // 3. 하나의 단체 챌린지 카테고리에서 10회 이상 인증 성공 → {카테고리명} 마스터
    for (GroupChallengeCategoryName categoryEnum : requiredCategories) {
      groupChallengeCategoryRepository
          .findByName(categoryEnum.name())
          .ifPresent(
              category -> {
                long count =
                    groupVerificationRepository.countByMemberIdAndCategoryAndStatus(
                        member.getId(), category, ChallengeStatus.SUCCESS);
                if (count >= 10) {
                  String badgeName = categoryEnum.getLabel() + " 마스터";
                  addIfNotExists(member, badgeName, newBadges);
                }
              });
    }

    // 4. 30일 연속 개인 챌린지 인증 성공 → 에코 슈퍼루키
    int streakDays = personalVerificationRepository.countConsecutiveSuccessDays(member.getId());
    if (streakDays >= 30) {
      addIfNotExists(member, "에코 슈퍼루키", newBadges);
    }

    return newBadges;
  }

  private void addIfNotExists(Member member, String badgeName, List<Badge> newBadges) {
    badgeRepository
        .findByName(badgeName)
        .ifPresent(
            badge -> {
              if (!memberBadgeRepository.existsByMemberAndBadge(member, badge)) {
                newBadges.add(badge);
              }
            });
  }
}
