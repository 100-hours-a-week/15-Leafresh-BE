package ktb.leafresh.backend.domain.store.product.infrastructure.cache;

import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.dto.ProductSummaryCacheDto;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.dto.TimedealProductSummaryCacheDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 일반 상품 캐시 등록
     */
    public void updateSingleProductCache(Product product) {
        String key = ProductCacheKeys.single(product.getId());
        ProductSummaryCacheDto dto = ProductSummaryCacheDtoMapper.from(product);

        redisTemplate.opsForValue().set(key, dto);
        log.info("[ProductCacheService] 일반 상품 캐시 저장 - key={}", key);

        double score = product.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        redisTemplate.opsForZSet().add(ProductCacheKeys.PRODUCT_SORTED_SET, product.getId(), score);
        log.info("[ProductCacheService] 일반 상품 ZSet 동기화 - productId={}, score={}", product.getId(), score);
    }

    /**
     * 타임딜 단건 캐시 등록
     */
    public void updateSingleTimedealCache(TimedealPolicy policy) {
        Product product = policy.getProduct();
        String key = ProductCacheKeys.timedealSingle(policy.getId());
        TimedealProductSummaryCacheDto dto = TimedealProductSummaryCacheDtoMapper.from(product, policy);

        Duration ttl = calculateTtl(policy.getEndTime().plusMinutes(1));
        if (ttl != null) {
            redisTemplate.opsForValue().set(key, dto, ttl);
            log.info("[ProductCacheService] 타임딜 단건 캐시 저장 - key={}, TTL={}초", key, ttl.getSeconds());
        } else {
            redisTemplate.opsForValue().set(key, dto);
            log.warn("[ProductCacheService] TTL 없이 타임딜 캐시 저장 - key={}", key);
        }

        updateTimedealZSet(policy);

        redisTemplate.delete(ProductCacheKeys.TIMEDEAL_LIST);
        log.info("[ProductCacheService] 타임딜 목록 캐시 무효화 - key={}", ProductCacheKeys.TIMEDEAL_LIST);
    }

    /**
     * 타임딜 ZSet 등록
     */
    public void updateTimedealZSet(TimedealPolicy policy) {
        long score = policy.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        redisTemplate.opsForZSet().add(ProductCacheKeys.TIMEDEAL_ZSET, policy.getId(), score);
        log.info("[ProductCacheService] 타임딜 ZSet 등록 - policyId={}, score={}", policy.getId(), score);
    }

    /**
     * 기존 모든 캐시 제거
     */
    public void evictCacheByProduct(Product product) {
        redisTemplate.delete(ProductCacheKeys.single(product.getId()));
        redisTemplate.opsForZSet().remove(ProductCacheKeys.PRODUCT_SORTED_SET, product.getId());

        log.info("[ProductCacheService] 일반 상품 캐시 및 ZSet 제거 - productId={}", product.getId());

        if (product.getActiveTimedealPolicy(LocalDateTime.now()).isPresent()) {
            redisTemplate.opsForZSet().remove(ProductCacheKeys.TIMEDEAL_ZSET, product.getId());
            redisTemplate.delete(ProductCacheKeys.TIMEDEAL_ACTIVE);
            log.info("[ProductCacheService] 타임딜 ZSet 및 ACTIVE 캐시 제거 - productId={}", product.getId());
        }
    }

    /**
     * 타임딜 캐시 제거 (단건 + ZSet)
     */
    public void evictTimedealCache(TimedealPolicy policy) {
        redisTemplate.delete(ProductCacheKeys.timedealSingle(policy.getId()));
        redisTemplate.opsForZSet().remove(ProductCacheKeys.TIMEDEAL_ZSET, policy.getId());

        log.info("[ProductCacheService] 타임딜 캐시 제거 - policyId={}", policy.getId());
    }

    /**
     * TTL 계산 (현재 시점 기준으로 endTime까지 남은 시간)
     */
    private Duration calculateTtl(LocalDateTime expireTime) {
        Duration ttl = Duration.between(LocalDateTime.now(), expireTime);
        return ttl.isNegative() || ttl.isZero() ? null : ttl;
    }

    /**
     * 일반 상품 재고 캐시 저장
     */
    public void cacheProductStock(Long productId, Integer stock) {
        String key = ProductCacheKeys.productStock(productId);
        redisTemplate.opsForValue().set(key, stock);
        log.info("[ProductCacheService] 일반 상품 재고 캐시 저장 - key={}, stock={}", key, stock);
    }

    /**
     * 타임딜 상품 재고 캐시 저장 (TTL 포함)
     */
    public void cacheTimedealStock(Long policyId, Integer stock, LocalDateTime endTime) {
        String key = ProductCacheKeys.timedealStock(policyId);
        Duration ttl = calculateTtl(endTime.plusMinutes(1));
        log.info("[ProductCacheService] 타임딜 TTL = {}", ttl != null ? ttl.getSeconds() : "null");

        if (ttl != null) {
            redisTemplate.opsForValue().set(key, stock, ttl);
            log.info("[ProductCacheService] 타임딜 재고 캐시 저장 - key={}, stock={}, TTL={}초", key, stock, ttl.getSeconds());
        } else {
            redisTemplate.opsForValue().set(key, stock);
            log.warn("[ProductCacheService] TTL 없이 타임딜 재고 캐시 저장 - key={}, stock={}", key, stock);
        }
    }
}
