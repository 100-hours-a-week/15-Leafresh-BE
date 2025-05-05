package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.service.GroupChallengeParticipantManager;
import ktb.leafresh.backend.domain.challenge.group.domain.support.policy.GroupChallengePromotionPolicy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupChallengeParticipationService {

    private final GroupChallengeParticipantManager participantManager;
    private final GroupChallengePromotionPolicy promotionPolicy;

    @Transactional
    public Long participate(Long memberId, Long challengeId) {
        return participantManager.participate(memberId, challengeId);
    }

    @Transactional
    public void drop(Long memberId, Long challengeId) {
        participantManager.drop(memberId, challengeId);
        promotionPolicy.promoteNextWaitingParticipant(challengeId);
    }
}
