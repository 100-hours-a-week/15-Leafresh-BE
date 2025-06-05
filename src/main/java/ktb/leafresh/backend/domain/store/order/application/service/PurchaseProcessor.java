package ktb.leafresh.backend.domain.store.order.application.service;

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
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.TimedealPolicyRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ProductErrorCode;
import ktb.leafresh.backend.global.exception.PurchaseErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional
    public void process(PurchaseProcessContext context) {
        Member member = context.member();
        Product product = context.product();
        int totalPrice = context.totalPrice();
        int quantity = context.quantity();

        log.debug("[검증] 재고 및 포인트 확인");
        if (context.purchaseType() == PurchaseType.TIMEDEAL) {
            // 타임딜일 경우 TimedealPolicy의 stock을 차감해야 함
            TimedealPolicy policy = product.getTimedealPolicies().stream()
                    .filter(p -> !p.isDeleted() && p.getStartTime().isBefore(LocalDateTime.now()) && p.getEndTime().isAfter(LocalDateTime.now()))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
            policy.decreaseStock(quantity);
            timedealPolicyRepository.save(policy); // dirty checking 우회를 위한 명시적 save
        } else {
            product.decreaseStock(quantity);
        }

        if (member.getCurrentLeafPoints() < totalPrice)
            throw new CustomException(PurchaseErrorCode.INSUFFICIENT_POINTS);

        log.debug("[차감] 포인트 및 재고 차감");
        member.updateCurrentLeafPoints(member.getCurrentLeafPoints() - totalPrice);
        productRepository.save(product); // dirty checking 방지

        log.debug("[저장] 구매 정보 저장");
        ProductPurchase purchase = ProductPurchase.builder()
                .member(member)
                .product(product)
                .quantity(quantity)
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
}
