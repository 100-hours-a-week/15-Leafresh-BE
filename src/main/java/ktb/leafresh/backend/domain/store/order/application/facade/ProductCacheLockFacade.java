package ktb.leafresh.backend.domain.store.order.application.facade;

import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.ProductCacheService;
import ktb.leafresh.backend.global.lock.annotation.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductCacheLockFacade {

    private final ProductCacheService productCacheService;

    @DistributedLock(key = "#product.id.toString()")
    public void updateSingleProductCache(Product product) {
        productCacheService.updateSingleProductCache(product);
    }

    @DistributedLock(key = "#policy.id.toString()")
    public void updateSingleTimedealCache(TimedealPolicy policy) {
        productCacheService.updateSingleTimedealCache(policy);
    }

    @DistributedLock(key = "#product.id.toString()")
    public void evictCacheByProduct(Product product) {
        productCacheService.evictCacheByProduct(product);
    }

    @DistributedLock(key = "#policy.id.toString()")
    public void evictTimedealCache(TimedealPolicy policy) {
        productCacheService.evictTimedealCache(policy);
    }

    @DistributedLock(key = "#productId.toString()")
    public void cacheProductStock(Long productId, Integer stock) {
        productCacheService.cacheProductStock(productId, stock);
    }

    @DistributedLock(key = "#policyId.toString()")
    public void cacheTimedealStock(Long policyId, Integer stock, LocalDateTime endTime) {
        productCacheService.cacheTimedealStock(policyId, stock, endTime);
    }
}
