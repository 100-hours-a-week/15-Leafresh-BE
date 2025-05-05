package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.global.common.entity.enums.ParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupChallengeParticipantRecordRepository extends JpaRepository<GroupChallengeParticipantRecord, Long> {
    boolean existsByGroupChallengeIdAndDeletedAtIsNull(Long groupChallengeId);

    boolean existsByGroupChallengeIdAndMemberIdAndDeletedAtIsNull(Long groupChallengeId, Long memberId);

    Optional<GroupChallengeParticipantRecord> findFirstByGroupChallengeIdAndStatusOrderByCreatedAtAsc(Long challengeId, ParticipantStatus status);

    Optional<GroupChallengeParticipantRecord> findByGroupChallengeIdAndMemberIdAndDeletedAtIsNull(Long groupChallengeId, Long memberId);
}
