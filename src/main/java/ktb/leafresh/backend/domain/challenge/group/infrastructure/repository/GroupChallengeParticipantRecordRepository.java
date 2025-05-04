package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupChallengeParticipantRecordRepository extends JpaRepository<GroupChallengeParticipantRecord, Long> {
    boolean existsByGroupChallengeIdAndDeletedAtIsNull(Long groupChallengeId);
}
