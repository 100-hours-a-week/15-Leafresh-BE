package ktb.leafresh.backend.domain.feedback.infrastructure.repository;

import ktb.leafresh.backend.domain.feedback.domain.entity.FeedbackFailureLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackFailureLogRepository extends JpaRepository<FeedbackFailureLog, Long> {
}
