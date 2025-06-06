package ktb.leafresh.backend.domain.verification.infrastructure.repository;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.verification.application.dto.VerificationStatSnapshot;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GroupChallengeVerificationRepository extends JpaRepository<GroupChallengeVerification, Long> {

    /**
     * 특정 회원이 특정 단체 챌린지에 대해 마지막으로 인증한 기록을 조회
     */
    Optional<GroupChallengeVerification> findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdOrderByCreatedAtDesc(
            Long memberId,
            Long challengeId
    );

    /**
     * 단체 챌린지 상세 페이지에 보여줄 최신 인증 이미지 9개 조회
     */
    List<GroupChallengeVerification> findTop9ByParticipantRecord_GroupChallenge_IdOrderByCreatedAtDesc(Long challengeId);

    Optional<GroupChallengeVerification> findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdAndCreatedAtBetween(
            Long memberId,
            Long challengeId,
            LocalDateTime start,
            LocalDateTime end
    );

    /**
     * 카테고리별 서로 다른 챌린지 인증 개수 (3개 이상일 때 사용)
     */
    @Query("""
    SELECT COUNT(DISTINCT gcv.participantRecord.groupChallenge.id)
    FROM GroupChallengeVerification gcv
    WHERE gcv.participantRecord.member.id = :memberId
      AND gcv.participantRecord.groupChallenge.category = :category
      AND gcv.status = :status
    """)
    long countDistinctChallengesByMemberIdAndCategoryAndStatus(
            @Param("memberId") Long memberId,
            @Param("category") GroupChallengeCategory category,
            @Param("status") ChallengeStatus status
    );

    /**
     * 카테고리별 인증 횟수 (10회 이상 마스터 조건)
     */
    @Query("""
    SELECT COUNT(gcv)
    FROM GroupChallengeVerification gcv
    WHERE gcv.participantRecord.member.id = :memberId
      AND gcv.participantRecord.groupChallenge.category = :category
      AND gcv.status = :status
    """)
    long countByMemberIdAndCategoryAndStatus(
            @Param("memberId") Long memberId,
            @Param("category") GroupChallengeCategory category,
            @Param("status") ChallengeStatus status
    );


    /**
     * 카테고리별 인증 1회 이상 여부 (지속가능 전도사 조건)
     */
    @Query("""
    SELECT CASE WHEN COUNT(gcv) > 0 THEN true ELSE false END
    FROM GroupChallengeVerification gcv
    WHERE gcv.participantRecord.member.id = :memberId
      AND gcv.participantRecord.groupChallenge.category = :category
      AND gcv.status = :status
    """)
    boolean existsByMemberIdAndCategoryAndStatus(
            @Param("memberId") Long memberId,
            @Param("category") GroupChallengeCategory category,
            @Param("status") ChallengeStatus status
    );

    /**
     * 이벤트 챌린지 제목 목록 조회
     */
    @Query("""
    SELECT DISTINCT gcv.participantRecord.groupChallenge.title
    FROM GroupChallengeVerification gcv
    WHERE gcv.participantRecord.groupChallenge.eventFlag = true
    """)
    List<String> findDistinctEventTitlesWithEventFlagTrue();

    /**
     * 특정 이벤트명에 대한 인증 횟수
     */
    @Query("""
    SELECT COUNT(gcv)
    FROM GroupChallengeVerification gcv
    WHERE gcv.participantRecord.member.id = :memberId
      AND gcv.participantRecord.groupChallenge.title = :eventTitle
      AND gcv.participantRecord.groupChallenge.eventFlag = true
      AND gcv.status = :status
    """)
    long countByMemberIdAndEventTitleAndStatus(
            @Param("memberId") Long memberId,
            @Param("eventTitle") String eventTitle,
            @Param("status") ChallengeStatus status
    );

    @Query("""
    SELECT COUNT(gcv)
    FROM GroupChallengeVerification gcv
    WHERE gcv.participantRecord.member.id = :memberId
      AND gcv.status = :status
    """)
    long countByMemberIdAndStatus(
            @Param("memberId") Long memberId,
            @Param("status") ChallengeStatus status
    );

    @Query("""
    SELECT gcv
    FROM GroupChallengeVerification gcv
    WHERE gcv.participantRecord.id IN :recordIds
    """)
    List<GroupChallengeVerification> findAllByParticipantRecordIds(@Param("recordIds") List<Long> recordIds);

    @Modifying
    @Query("""
    UPDATE GroupChallengeVerification v
    SET v.viewCount = v.viewCount + :view,
        v.likeCount = v.likeCount + :like,
        v.commentCount = v.commentCount + :comment
    WHERE v.id = :id
    """)
    void updateCounts(@Param("id") Long id,
                      @Param("view") int view,
                      @Param("like") int like,
                      @Param("comment") int comment);

    Optional<GroupChallengeVerification> findByIdAndDeletedAtIsNull(Long id);

    @Query("""
    SELECT v.id, v.viewCount
    FROM GroupChallengeVerification v
    WHERE v.deletedAt IS NULL
    """)
    List<Object[]> findAllViewCountByVerificationId();

    @Modifying
    @Query("UPDATE GroupChallengeVerification g SET " +
            "g.viewCount = g.viewCount + :viewCount, " +
            "g.likeCount = g.likeCount + :likeCount, " +
            "g.commentCount = g.commentCount + :commentCount " +
            "WHERE g.id = :verificationId")
    void increaseCounts(@Param("verificationId") Long verificationId,
                        @Param("viewCount") int viewCount,
                        @Param("likeCount") int likeCount,
                        @Param("commentCount") int commentCount);

    @Query("SELECT new ktb.leafresh.backend.domain.verification.application.dto.VerificationStatSnapshot(" +
            "v.id, v.viewCount, v.likeCount, v.commentCount) " +
            "FROM GroupChallengeVerification v WHERE v.id = :id")
    Optional<VerificationStatSnapshot> findStatById(@Param("id") Long id);
}
