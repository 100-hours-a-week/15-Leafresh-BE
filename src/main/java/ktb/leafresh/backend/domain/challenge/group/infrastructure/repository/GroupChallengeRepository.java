package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GroupChallengeRepository extends JpaRepository<GroupChallenge, Long> {

  @Query(
      "SELECT gc FROM GroupChallenge gc "
          + "WHERE gc.endDate >= :today "
          + "AND gc.deletedAt IS NULL")
  List<GroupChallenge> findAllValidAndOngoing(@Param("today") LocalDateTime today);

  @Query(
      "SELECT gc FROM GroupChallenge gc "
          + "WHERE gc.eventFlag = true "
          + "AND gc.deletedAt IS NULL "
          + "AND gc.startDate <= :endInclusive "
          + "AND gc.endDate >= :now")
  List<GroupChallenge> findEventChallengesWithinRange(
      @Param("now") LocalDateTime now, @Param("endInclusive") LocalDateTime endInclusive);

  boolean existsByTitleAndEventFlagTrue(String title);
}
