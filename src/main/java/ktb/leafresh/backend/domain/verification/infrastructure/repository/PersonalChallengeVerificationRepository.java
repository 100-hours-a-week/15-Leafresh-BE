package ktb.leafresh.backend.domain.verification.infrastructure.repository;

import ktb.leafresh.backend.domain.verification.domain.entity.PersonalChallengeVerification;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PersonalChallengeVerificationRepository extends JpaRepository<PersonalChallengeVerification, Long>,
        PersonalChallengeVerificationCustomRepository {

    Optional<PersonalChallengeVerification> findTopByMemberIdAndPersonalChallengeIdAndCreatedAtBetween(
            Long memberId,
            Long challengeId,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    int countConsecutiveSuccessDays(Long memberId);

    /**
     * 전체 개인 챌린지 제목 조회
     */
    @Query("""
    SELECT DISTINCT pcv.personalChallenge.title
    FROM PersonalChallengeVerification pcv
    """)
    List<String> findAllPersonalChallengeTitles();

    /**
     * 특정 개인 챌린지 인증 성공 여부
     */
    @Query("""
    SELECT CASE WHEN COUNT(pcv) > 0 THEN true ELSE false END
    FROM PersonalChallengeVerification pcv
    WHERE pcv.member.id = :memberId
      AND pcv.personalChallenge.title = :title
      AND pcv.status = :status
    """)
    boolean existsByMemberIdAndPersonalChallengeTitleAndStatus(
            @Param("memberId") Long memberId,
            @Param("title") String title,
            @Param("status") ChallengeStatus status
    );

    /**
     * 전체 개인 인증 횟수
     */
    @Query("""
    SELECT COUNT(pcv)
    FROM PersonalChallengeVerification pcv
    WHERE pcv.member.id = :memberId
      AND pcv.status = :status
    """)
    long countTotalByMemberIdAndStatus(
            @Param("memberId") Long memberId,
            @Param("status") ChallengeStatus status
    );

    @Query("""
    SELECT pcv
    FROM PersonalChallengeVerification pcv
    WHERE pcv.member.id = :memberId
      AND pcv.verifiedAt BETWEEN :start AND :end
    """)
    List<PersonalChallengeVerification> findWeeklyVerifications(
            @Param("memberId") Long memberId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
