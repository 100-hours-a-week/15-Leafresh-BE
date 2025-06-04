package ktb.leafresh.backend.domain.store.order.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.store.order.application.dto.PurchaseCommand;
import ktb.leafresh.backend.domain.store.order.domain.entity.*;
import ktb.leafresh.backend.domain.store.order.domain.entity.enums.PurchaseProcessingStatus;
import ktb.leafresh.backend.domain.store.order.domain.entity.enums.PurchaseType;
import ktb.leafresh.backend.domain.store.order.infrastructure.repository.*;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
import ktb.leafresh.backend.global.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPurchaseProcessingService {

    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final ProductPurchaseRepository purchaseRepository;
    private final PurchaseProcessingLogRepository processingLogRepository;
    private final PurchaseFailureLogRepository failureLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void process(PurchaseCommand cmd) {
        log.info("[구매 처리 시작] memberId={}, productId={}, quantity={}",
                cmd.memberId(), cmd.productId(), cmd.quantity());

        try {
            log.debug("1. 사용자 조회 시작");
            Member member = memberRepository.findById(cmd.memberId())
                    .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
            log.debug("1. 사용자 조회 완료: {}", member.getNickname());

            log.debug("2. 상품 조회 시작");
            Product product = productRepository.findById(cmd.productId())
                    .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
            log.debug("2. 상품 조회 완료: {}", product.getName());

            log.debug("3. 재고 및 포인트 검증 시작");
            if (product.getStock() < cmd.quantity()) {
                throw new CustomException(PurchaseErrorCode.INSUFFICIENT_STOCK);
            }

            int totalPrice = product.getPrice() * cmd.quantity();

            if (member.getCurrentLeafPoints() < totalPrice) {
                throw new CustomException(PurchaseErrorCode.INSUFFICIENT_POINTS);
            }
            log.debug("3. 검증 통과 - 현재 재고: {}, 보유 포인트: {}", product.getStock(), member.getCurrentLeafPoints());

            log.debug("4. 포인트 차감 및 재고 차감 시작");
            member.updateCurrentLeafPoints(member.getCurrentLeafPoints() - totalPrice);
            product.updateStock(product.getStock() - cmd.quantity());
            productRepository.save(product); // 명시적 저장 (dirty checking 이슈 예방)
            log.debug("4. 차감 완료 - 남은 재고: {}, 남은 포인트: {}", product.getStock(), member.getCurrentLeafPoints());

            log.debug("5. 구매 정보 저장 시작");
            ProductPurchase purchase = ProductPurchase.builder()
                    .member(member)
                    .product(product)
                    .quantity(cmd.quantity())
                    .price(product.getPrice())
                    .type(PurchaseType.NORMAL)
                    .purchasedAt(LocalDateTime.now())
                    .build();
            purchaseRepository.save(purchase);
            log.debug("5. 구매 정보 저장 완료: purchaseId={}", purchase.getId());

            log.debug("6. 성공 로그 저장 시작");
            processingLogRepository.save(PurchaseProcessingLog.builder()
                    .product(product)
                    .status(PurchaseProcessingStatus.SUCCESS)
                    .message("구매 성공")
                    .build());
            log.debug("6. 성공 로그 저장 완료");

            log.info("[구매 처리 완료] memberId={}, productId={}, price={}, points left={}",
                    cmd.memberId(), cmd.productId(), totalPrice, member.getCurrentLeafPoints());

        } catch (Exception e) {
            log.warn("예외 발생 - 실패 로그 저장 시작");

            String requestBodyJson;
            try {
                requestBodyJson = objectMapper.writeValueAsString(cmd);
            } catch (JsonProcessingException jsonException) {
                requestBodyJson = String.format("{\"fallback\": \"%s\"}", cmd.toString().replace("\"", "\\\""));
            }

            failureLogRepository.save(PurchaseFailureLog.builder()
                    .member(Member.builder().id(cmd.memberId()).build())
                    .product(Product.builder().id(cmd.productId()).build())
                    .reason(e.getMessage())
                    .requestBody(requestBodyJson)
                    .occurredAt(LocalDateTime.now())
                    .build());

            processingLogRepository.save(PurchaseProcessingLog.builder()
                    .product(Product.builder().id(cmd.productId()).build())
                    .status(PurchaseProcessingStatus.FAILURE)
                    .message(e.getMessage())
                    .build());

            log.error("[구매 처리 실패] {}", e.getMessage(), e);
            throw e;
        }
    }
}
