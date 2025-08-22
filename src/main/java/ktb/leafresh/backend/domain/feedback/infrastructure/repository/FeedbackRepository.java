package ktb.leafresh.backend.domain.feedback.infrastructure.repository;

import ktb.leafresh.backend.domain.feedback.domain.entity.Feedback;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

  @Query(
      "SELECT f FROM Feedback f WHERE f.member = :member AND f.weekMonday = :week AND f.deletedAt IS NULL")
  Optional<Feedback> findFeedbackByMemberAndWeekMonday(
      @Param("member") Member member, @Param("week") LocalDateTime weekMonday);

  boolean existsByMemberIdAndWeekMonday(Long memberId, LocalDateTime weekMonday);
}
