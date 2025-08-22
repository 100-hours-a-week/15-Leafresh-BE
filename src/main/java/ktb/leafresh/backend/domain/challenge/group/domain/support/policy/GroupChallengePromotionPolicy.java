package ktb.leafresh.backend.domain.challenge.group.domain.support.policy;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeParticipantRecordRepository;
import ktb.leafresh.backend.global.common.entity.enums.ParticipantStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GroupChallengePromotionPolicy {

  private final GroupChallengeParticipantRecordRepository participantRepository;

  /** 대기자 중 가장 오래된 사람을 ACTIVE로 승격시키고, 현재 인원 수 증가 */
  @Transactional
  public void promoteNextWaitingParticipant(Long challengeId) {
    participantRepository
        .findFirstByGroupChallengeIdAndStatusOrderByCreatedAtAsc(
            challengeId, ParticipantStatus.WAITING)
        .ifPresent(
            waiting -> {
              waiting.changeStatus(ParticipantStatus.ACTIVE);
              GroupChallenge challenge = waiting.getGroupChallenge();
              challenge.increaseParticipantCount();
            });
  }
}
