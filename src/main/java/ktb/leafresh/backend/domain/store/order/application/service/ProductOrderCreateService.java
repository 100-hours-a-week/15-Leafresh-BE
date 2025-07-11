package ktb.leafresh.backend.domain.store.order.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.store.order.application.facade.ProductCacheLockFacade;
import ktb.leafresh.backend.domain.store.order.domain.entity.PurchaseIdempotencyKey;
import ktb.leafresh.backend.domain.store.order.infrastructure.publisher.PurchaseMessagePublisher;
import ktb.leafresh.backend.domain.store.order.infrastructure.repository.PurchaseIdempotencyKeyRepository;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.ProductCacheKeys;
import ktb.leafresh.backend.global.exception.*;
import ktb.leafresh.backend.global.lock.annotation.DistributedLock;
import ktb.leafresh.backend.global.util.redis.StockRedisLuaService;
import ktb.leafresh.backend.domain.store.order.application.dto.PurchaseCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductOrderCreateService {

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final PurchaseIdempotencyKeyRepository idempotencyRepository;
    private final StockRedisLuaService stockRedisLuaService;
    private final PurchaseMessagePublisher purchaseMessagePublisher;
    private final ProductCacheLockFacade productCacheLockFacade;

    @DistributedLock(key = "'product:stock:' + #productId", waitTime = 0, leaseTime = 3)
    @Transactional
    public void create(Long memberId, Long productId, int quantity, String idempotencyKey) {
        // 1. 사용자 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 2. Idempotency 키 저장
        try {
            idempotencyRepository.save(new PurchaseIdempotencyKey(member, idempotencyKey));
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(PurchaseErrorCode.DUPLICATE_PURCHASE_REQUEST);
        }

        // 3. 상품 존재 여부 확인 (재고 키 조회용)
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        // 3-1. 캐시 없으면 Redisson Lock 기반으로 캐싱 (스탬피드 방지)
        productCacheLockFacade.cacheProductStock(productId, product.getStock());

        // 4. Redis 재고 선점
        String redisKey = ProductCacheKeys.productStock(productId);
        Long result = stockRedisLuaService.decreaseStock(redisKey, quantity);

        if (result == -1) throw new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND);
        if (result == -2) throw new CustomException(ProductErrorCode.OUT_OF_STOCK);

        // 5. 메시지 큐 발행
        PurchaseCommand command = new PurchaseCommand(memberId, productId, null, quantity, idempotencyKey, LocalDateTime.now());
        purchaseMessagePublisher.publish(command);

        log.info("[주문 큐 발행 완료] memberId={}, productId={}, quantity={}", memberId, productId, quantity);
    }
}
