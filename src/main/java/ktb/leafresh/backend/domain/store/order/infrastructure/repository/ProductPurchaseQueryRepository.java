package ktb.leafresh.backend.domain.store.order.infrastructure.repository;

import ktb.leafresh.backend.domain.store.order.domain.entity.ProductPurchase;

import java.util.List;

public interface ProductPurchaseQueryRepository {
    List<ProductPurchase> findByMemberWithCursorAndSearch(Long memberId, String input, Long cursorId, String cursorTimestamp, int size);
}
