package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.global.common.entity.enums.ParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GroupChallengeParticipantRecordRepository extends JpaRepository<GroupChallengeParticipantRecord, Long> {
    boolean existsByGroupChallengeIdAndDeletedAtIsNull(Long groupChallengeId);

    boolean existsByGroupChallengeIdAndMemberIdAndDeletedAtIsNull(Long groupChallengeId, Long memberId);

    Optional<GroupChallengeParticipantRecord> findFirstByGroupChallengeIdAndStatusOrderByCreatedAtAsc(Long challengeId, ParticipantStatus status);

    Optional<GroupChallengeParticipantRecord> findByGroupChallengeIdAndMemberIdAndDeletedAtIsNull(Long groupChallengeId, Long memberId);

    Optional<GroupChallengeParticipantRecord> findByMemberIdAndGroupChallengeIdAndDeletedAtIsNull(Long memberId, Long challengeId);

    @Query("""
    SELECT CASE WHEN COUNT(pr) > 0 THEN true ELSE false END
    FROM GroupChallengeParticipantRecord pr
    WHERE pr.member.id = :memberId
    AND pr.createdAt >= :oneWeekAgo
    AND pr.status = 'ACTIVE'
    AND pr.deletedAt IS NULL
    """)
    boolean existsSuccessInPastWeek(@Param("memberId") Long memberId, @Param("oneWeekAgo") LocalDateTime oneWeekAgo);

    List<GroupChallengeParticipantRecord> findAllByMemberId(Long memberId);
}
