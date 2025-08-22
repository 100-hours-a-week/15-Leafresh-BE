package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.member.application.service.updater.LeafPointCacheUpdater;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import ktb.leafresh.backend.domain.member.infrastructure.repository.TreeLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RewardGrantService {

  private static final int SIGNUP_REWARD = 1500;
  private static final int DAILY_LOGIN_REWARD = 30;
  private final LeafPointCacheUpdater rewardService;
  private final TreeLevelRepository treeLevelRepository;

  public void grantLeafPoints(Member member, int points) {
    member.addLeafPoints(points);
    rewardService.rewardLeafPoints(member, points);
    updateTreeLevelIfNeeded(member);
    log.info(
        "[나뭇잎 지급] memberId={}, 지급량={}, 현재={}, 누적={}, 트리레벨={}",
        member.getId(),
        points,
        member.getCurrentLeafPoints(),
        member.getTotalLeafPoints(),
        member.getTreeLevel().getName());
  }

  public void grantParticipationBonus(Member member, GroupChallengeParticipantRecord record) {
    int days = record.getGroupChallenge().getDurationInDays();
    int bonus = calculateBonus(days);
    grantLeafPoints(member, bonus);
    log.info("[전체 인증 성공 보너스 지급] memberId={}, days={}, bonus={}", member.getId(), days, bonus);
  }

  public void grantSignupReward(Member member) {
    grantLeafPoints(member, SIGNUP_REWARD);
    log.info("[회원가입 보상 지급] memberId={}, 보상={}", member.getId(), SIGNUP_REWARD);
  }

  public void grantDailyLoginReward(Member member) {
    if (member.hasReceivedLoginRewardToday()) {
      log.info("[일일 로그인 보상 스킵] 오늘 이미 보상받은 사용자입니다. memberId={}", member.getId());
      return;
    }

    grantLeafPoints(member, DAILY_LOGIN_REWARD);
    member.updateLastLoginRewardedAt();
    log.info(
        "[일일 로그인 보상 지급] memberId={}, 보상={}, 현재={}, 누적={}",
        member.getId(),
        DAILY_LOGIN_REWARD,
        member.getCurrentLeafPoints(),
        member.getTotalLeafPoints());
  }

  private int calculateBonus(int days) {
    if (days <= 5) return 50;
    else if (days <= 10) return 100;
    else if (days <= 15) return 150;
    else return 200;
  }

  private void updateTreeLevelIfNeeded(Member member) {
    TreeLevel newLevel =
        treeLevelRepository
            .findFirstByMinLeafPointLessThanEqualOrderByMinLeafPointDesc(
                member.getTotalLeafPoints())
            .orElseThrow(() -> new IllegalStateException("적절한 TreeLevel을 찾을 수 없습니다."));

    if (!member.getTreeLevel().equals(newLevel)) {
      TreeLevel oldLevel = member.getTreeLevel();
      member.updateTreeLevel(newLevel);
      log.info(
          "[트리 레벨 변경됨] memberId={}, {} → {}",
          member.getId(),
          oldLevel.getName(),
          newLevel.getName());
    }
  }
}
