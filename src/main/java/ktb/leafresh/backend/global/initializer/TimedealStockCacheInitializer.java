package ktb.leafresh.backend.global.initializer;

import jakarta.annotation.PostConstruct;
import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.ProductCacheService;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.TimedealPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimedealStockCacheInitializer {

    private final TimedealPolicyRepository timedealPolicyRepository;
    private final ProductCacheService productCacheService;

    @PostConstruct
    public void initTimedealStockCache() {
        List<TimedealPolicy> validPolicies = timedealPolicyRepository.findAllValidWithProduct(LocalDateTime.now());

        int successCount = 0;
        for (TimedealPolicy policy : validPolicies) {
            try {
                productCacheService.cacheTimedealStock(
                        policy.getId(),
                        policy.getStock(),
                        policy.getEndTime()
                );
                productCacheService.updateSingleTimedealCache(policy);
                successCount++;
            } catch (Exception e) {
                log.error("[TimedealStockCacheInitializer] 캐시 등록 실패 - timedealId={}", policy.getId(), e);
            }
        }

        log.info("[TimedealStockCacheInitializer] Redis 타임딜 재고 캐시 초기화 완료 - 총 {}건", successCount);
    }
}
