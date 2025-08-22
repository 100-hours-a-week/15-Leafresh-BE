package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.common.entity.enums.ParticipantStatus;

public class GroupChallengeParticipantRecordFixture {

  /** 기본 참여 상태인 ACTIVE 상태의 참가 기록 생성 */
  public static GroupChallengeParticipantRecord of(GroupChallenge challenge, Member member) {
    return of(challenge, member, ParticipantStatus.ACTIVE);
  }

  /** 전달받은 status로 참가 기록 생성 */
  public static GroupChallengeParticipantRecord of(
      GroupChallenge challenge, Member member, ParticipantStatus status) {
    return GroupChallengeParticipantRecord.create(member, challenge, status);
  }
}
