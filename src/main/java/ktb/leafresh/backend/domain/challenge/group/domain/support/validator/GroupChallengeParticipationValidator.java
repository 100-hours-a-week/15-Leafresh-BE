package ktb.leafresh.backend.domain.challenge.group.domain.support.validator;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeParticipantRecordRepository;
import ktb.leafresh.backend.global.common.entity.enums.ParticipantStatus;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import org.springframework.stereotype.Component;

@Component
public class GroupChallengeParticipationValidator {

  private final GroupChallengeParticipantRecordRepository participantRepository;

  public GroupChallengeParticipationValidator(
      GroupChallengeParticipantRecordRepository participantRepository) {
    this.participantRepository = participantRepository;
  }

  // 이미 참여했는지 확인
  public void validateNotAlreadyParticipated(Long challengeId, Long memberId) {
    if (participantRepository.existsByGroupChallengeIdAndMemberIdAndDeletedAtIsNull(
        challengeId, memberId)) {
      throw new CustomException(ChallengeErrorCode.CHALLENGE_ALREADY_PARTICIPATED);
    }
  }

  // DROPPED, FINISHED, BANNED는 중복 취소 불가
  public void validateDroppable(GroupChallengeParticipantRecord record) {
    ParticipantStatus status = record.getStatus();
    if (status == ParticipantStatus.DROPPED
        || status == ParticipantStatus.FINISHED
        || status == ParticipantStatus.BANNED) {
      throw new CustomException(ChallengeErrorCode.CHALLENGE_ALREADY_DROPPED);
    }
  }
}
