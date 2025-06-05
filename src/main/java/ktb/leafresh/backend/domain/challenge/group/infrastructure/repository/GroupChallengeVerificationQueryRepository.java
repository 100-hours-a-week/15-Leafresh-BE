package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;

import java.util.List;
import java.util.Optional;

public interface GroupChallengeVerificationQueryRepository {
    List<GroupChallengeVerification> findByChallengeId(Long challengeId, Long cursorId, String cursorTimestamp, int size);

    List<GroupChallengeVerification> findByParticipantRecordId(Long participantRecordId);

    Optional<GroupChallengeVerification> findByChallengeIdAndId(Long challengeId, Long verificationId);
}
