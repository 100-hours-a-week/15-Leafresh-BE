package ktb.leafresh.backend.domain.store.product.infrastructure.repository;

import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {}
