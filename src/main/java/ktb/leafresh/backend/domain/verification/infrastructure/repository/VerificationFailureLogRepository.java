package ktb.leafresh.backend.domain.verification.infrastructure.repository;

import ktb.leafresh.backend.domain.verification.domain.entity.VerificationFailureLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationFailureLogRepository extends JpaRepository<VerificationFailureLog, Long> {
}
