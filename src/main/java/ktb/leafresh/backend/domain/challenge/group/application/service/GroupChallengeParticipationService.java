package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.service.GroupChallengeParticipantManager;
import ktb.leafresh.backend.domain.challenge.group.domain.support.policy.GroupChallengePromotionPolicy;

import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupChallengeParticipationService {

    private final GroupChallengeParticipantManager participantManager;
    private final GroupChallengePromotionPolicy promotionPolicy;

    @Transactional
    public Long participate(Long memberId, Long challengeId) {
        try {
            return participantManager.participate(memberId, challengeId);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[단체 챌린지 참여 실패] challengeId={}, memberId={}, error={}", challengeId, memberId, e.getMessage(), e);
            throw new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_PARTICIPATION_FAILED);
        }
    }

    @Transactional
    public void drop(Long memberId, Long challengeId) {
        try {
            participantManager.drop(memberId, challengeId);
            promotionPolicy.promoteNextWaitingParticipant(challengeId);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[단체 챌린지 참여 취소 실패] challengeId={}, memberId={}, error={}", challengeId, memberId, e.getMessage(), e);
            throw new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_PARTICIPATION_FAILED);
        }
    }
}
