package ktb.leafresh.backend.domain.store.order.infrastructure.repository;

import ktb.leafresh.backend.domain.store.order.domain.entity.PurchaseProcessingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseProcessingLogRepository extends JpaRepository<PurchaseProcessingLog, Long> {
}
