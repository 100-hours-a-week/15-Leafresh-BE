package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface GroupChallengeRepository extends JpaRepository<GroupChallenge, Long> {

    @Query("SELECT gc FROM GroupChallenge gc " +
            "WHERE gc.endDate >= :today " +
            "AND gc.deletedAt IS NULL")
    List<GroupChallenge> findAllValidAndOngoing(@Param("today") LocalDateTime today);
}
