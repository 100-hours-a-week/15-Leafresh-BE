package ktb.leafresh.backend.domain.store.order.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.store.order.application.service.model.PurchaseProcessContext;
import ktb.leafresh.backend.domain.store.order.domain.entity.ProductPurchase;
import ktb.leafresh.backend.domain.store.order.domain.entity.PurchaseProcessingLog;
import ktb.leafresh.backend.domain.store.order.domain.entity.enums.PurchaseProcessingStatus;
import ktb.leafresh.backend.domain.store.order.domain.entity.enums.PurchaseType;
import ktb.leafresh.backend.domain.store.order.infrastructure.repository.ProductPurchaseRepository;
import ktb.leafresh.backend.domain.store.order.infrastructure.repository.PurchaseProcessingLogRepository;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.TimedealProductSummaryCacheDtoMapper;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.TimedealPolicyRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ProductErrorCode;
import ktb.leafresh.backend.global.exception.PurchaseErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class PurchaseProcessor {

    private final ProductPurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final TimedealPolicyRepository timedealPolicyRepository;
    private final PurchaseProcessingLogRepository processingLogRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void process(PurchaseProcessContext context) {
        log.debug("[검증] 재고 및 포인트 확인");
        if (context.purchaseType() == PurchaseType.TIMEDEAL) {
            processTimedealPurchase(context);
        } else {
            processNormalPurchase(context);
        }
    }

    private void processTimedealPurchase(PurchaseProcessContext context) {
        Member member = context.member();
        Product product = context.product();
        int quantity = context.quantity();
        int totalPrice = context.totalPrice();

        TimedealPolicy policy = product.getTimedealPolicies().stream()
                .filter(p -> !p.isDeleted()
                        && p.getStartTime().isBefore(LocalDateTime.now())
                        && p.getEndTime().isAfter(LocalDateTime.now()))
                .findFirst()
                .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        policy.decreaseStock(quantity);
        timedealPolicyRepository.save(policy); // dirty checking 우회

        finalizePurchase(context, policy);
        updateTimedealCache(product, policy);
    }

    private void processNormalPurchase(PurchaseProcessContext context) {
        Product product = context.product();
        int quantity = context.quantity();

        product.decreaseStock(quantity);
        productRepository.save(product); // dirty checking 방지

        finalizePurchase(context, null);
    }

    private void finalizePurchase(PurchaseProcessContext context, TimedealPolicy policy) {
        Member member = context.member();
        Product product = context.product();
        int totalPrice = context.totalPrice();

        if (member.getCurrentLeafPoints() < totalPrice) {
            throw new CustomException(PurchaseErrorCode.INSUFFICIENT_POINTS);
        }

        log.debug("[차감] 포인트 차감 및 구매 저장");
        member.updateCurrentLeafPoints(member.getCurrentLeafPoints() - totalPrice);

        ProductPurchase purchase = ProductPurchase.builder()
                .member(member)
                .product(product)
                .quantity(context.quantity())
                .price(context.unitPrice())
                .type(context.purchaseType())
                .purchasedAt(LocalDateTime.now())
                .build();
        purchaseRepository.save(purchase);

        processingLogRepository.save(PurchaseProcessingLog.builder()
                .product(product)
                .status(PurchaseProcessingStatus.SUCCESS)
                .message("구매 성공")
                .build());

        log.info("[구매 처리 완료] memberId={}, productId={}, price={}, points left={}",
                member.getId(), product.getId(), totalPrice, member.getCurrentLeafPoints());
    }

    private void updateTimedealCache(Product product, TimedealPolicy policy) {
        String itemKey = "store:products:timedeal:item:" + policy.getId();
        try {
            var cacheDto = TimedealProductSummaryCacheDtoMapper.from(product, policy);
            String json = objectMapper.writeValueAsString(cacheDto);
            redisTemplate.opsForValue().set(itemKey, json);
            log.debug("[Redis] 단건 캐시 갱신 완료 - key={}, stock={}", itemKey, cacheDto.stock());
        } catch (JsonProcessingException e) {
            log.error("[Redis] 단건 캐시 직렬화 실패 - policyId={}", policy.getId(), e);
        }
    }
}
