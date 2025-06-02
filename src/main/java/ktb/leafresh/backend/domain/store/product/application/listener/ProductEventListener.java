package ktb.leafresh.backend.domain.store.product.application.listener;

import ktb.leafresh.backend.domain.store.product.application.event.ProductUpdatedEvent;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.ProductCacheService;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductCacheService productCacheService;
    private final ProductRepository productRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductUpdated(ProductUpdatedEvent event) {
        Long productId = event.productId();

        productRepository.findById(productId).ifPresent(product -> {
            productCacheService.updateSingleProductCache(product);
            log.info("[ProductEventListener] 개별 상품 캐시 갱신 완료 - productId={}, isTimeDeal={}", productId, event.isTimeDeal());
        });
    }
}
