package ktb.leafresh.backend.domain.store.product.infrastructure.repository;

import ktb.leafresh.backend.domain.store.product.domain.entity.Product;

import java.util.List;

public interface ProductSearchQueryRepository {
    List<Product> findWithFilter(String input, Long cursorId, String cursorTimestamp, int size);
}
