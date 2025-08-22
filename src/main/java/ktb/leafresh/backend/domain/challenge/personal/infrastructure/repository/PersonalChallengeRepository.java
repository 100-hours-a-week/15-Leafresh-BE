package ktb.leafresh.backend.domain.challenge.personal.infrastructure.repository;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PersonalChallengeRepository extends JpaRepository<PersonalChallenge, Long> {
  int countByDayOfWeek(DayOfWeek dayOfWeek);

  List<PersonalChallenge> findAllByDayOfWeek(DayOfWeek dayOfWeek);

  @Query(
      """
    SELECT CASE WHEN COUNT(pc) > 0 THEN true ELSE false END
    FROM PersonalChallenge pc
    JOIN pc.verifications v
    WHERE v.member.id = :memberId
    AND v.status = 'SUCCESS'
    AND v.createdAt >= :oneWeekAgo
    AND pc.deletedAt IS NULL
    """)
  boolean existsSuccessInPastWeek(
      @Param("memberId") Long memberId, @Param("oneWeekAgo") LocalDateTime oneWeekAgo);
}
