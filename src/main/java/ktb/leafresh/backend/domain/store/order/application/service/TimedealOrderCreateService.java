package ktb.leafresh.backend.domain.store.order.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.store.order.application.dto.PurchaseCommand;
import ktb.leafresh.backend.domain.store.order.domain.entity.PurchaseIdempotencyKey;
import ktb.leafresh.backend.domain.store.order.infrastructure.publisher.PurchaseMessagePublisher;
import ktb.leafresh.backend.domain.store.order.infrastructure.repository.PurchaseIdempotencyKeyRepository;
import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.ProductCacheKeys;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.TimedealPolicyRepository;
import ktb.leafresh.backend.global.exception.*;
import ktb.leafresh.backend.global.util.redis.RedisLuaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimedealOrderCreateService {

    private final MemberRepository memberRepository;
    private final TimedealPolicyRepository timedealPolicyRepository;
    private final PurchaseIdempotencyKeyRepository idempotencyRepository;
    private final RedisLuaService redisLuaService;
    private final PurchaseMessagePublisher purchaseMessagePublisher;

    @Transactional
    public void create(Long memberId, Long dealId, int quantity, String idempotencyKey) {
        // 1. 사용자 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 2. Idempotency 키 저장
        try {
            idempotencyRepository.save(new PurchaseIdempotencyKey(member, idempotencyKey));
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(PurchaseErrorCode.DUPLICATE_PURCHASE_REQUEST);
        }

        // 3. 타임딜 정책 조회
        TimedealPolicy policy = timedealPolicyRepository.findById(dealId)
                .orElseThrow(() -> new CustomException(TimedealErrorCode.PRODUCT_NOT_FOUND));

        // 4. 구매 가능 시간 검증
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(policy.getStartTime()) || now.isAfter(policy.getEndTime())) {
            throw new CustomException(ProductErrorCode.INVALID_STATUS, "현재는 구매할 수 없는 시간입니다.");
        }

        // 5. 재고 선점 (Redis Lua)
        String redisKey = ProductCacheKeys.timedealStock(dealId);
        Long result = redisLuaService.decreaseStock(redisKey, quantity);

        if (result == -1) throw new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND);
        if (result == -2) throw new CustomException(ProductErrorCode.OUT_OF_STOCK);

        // 6. MQ 발행
        purchaseMessagePublisher.publish(new PurchaseCommand(
                memberId,
                policy.getProduct().getId(), // 실제 상품 ID
                policy.getId(),
                quantity,
                idempotencyKey,
                now
        ));

        log.info("[타임딜 주문 큐 발행 완료] memberId={}, policyId={}, productId={}, quantity={}",
                memberId, dealId, policy.getProduct().getId(), quantity);
    }
}
