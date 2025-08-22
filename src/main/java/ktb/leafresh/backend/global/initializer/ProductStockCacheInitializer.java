package ktb.leafresh.backend.global.initializer;

import jakarta.annotation.PostConstruct;
import ktb.leafresh.backend.domain.store.order.application.facade.ProductCacheLockFacade;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.ProductCacheService;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductStockCacheInitializer {

  private final ProductRepository productRepository;
  private final ProductCacheLockFacade productCacheLockFacade;

  /** 서비스 시작 시 모든 상품 재고를 Redis에 캐싱 */
  @PostConstruct
  public void initProductStockCache() {
    List<Product> products = productRepository.findAll();

    int successCount = 0;
    for (Product product : products) {
      try {
        productCacheLockFacade.cacheProductStock(product.getId(), product.getStock());
        successCount++;
      } catch (Exception e) {
        log.error("[ProductStockCacheInitializer] 캐시 등록 실패 - productId={}", product.getId(), e);
      }
    }

    log.info("[ProductStockCacheInitializer] Redis 재고 캐시 초기화 완료 - 총 {}건", successCount);
  }
}
