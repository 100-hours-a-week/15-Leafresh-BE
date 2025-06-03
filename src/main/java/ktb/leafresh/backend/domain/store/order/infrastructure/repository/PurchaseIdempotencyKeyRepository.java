package ktb.leafresh.backend.domain.store.order.infrastructure.repository;

import ktb.leafresh.backend.domain.store.order.domain.entity.PurchaseIdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseIdempotencyKeyRepository extends JpaRepository<PurchaseIdempotencyKey, Long> {
}
