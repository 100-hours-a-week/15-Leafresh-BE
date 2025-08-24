package ktb.leafresh.backend.domain.store.order.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.store.order.application.dto.PurchaseCommand;
import ktb.leafresh.backend.domain.store.order.application.service.model.PurchaseProcessContext;
import ktb.leafresh.backend.domain.store.order.domain.entity.*;
import ktb.leafresh.backend.domain.store.order.domain.entity.enums.PurchaseType;
import ktb.leafresh.backend.domain.store.order.infrastructure.repository.*;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.TimedealPolicyRepository;
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
  private final TimedealPolicyRepository timedealPolicyRepository;
  private final PurchaseFailureLogRepository failureLogRepository;
  private final PurchaseProcessor purchaseProcessor;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Transactional
  public void process(PurchaseCommand cmd) {
    try {
      // 회원 조회
      Member member =
          memberRepository
              .findById(cmd.memberId())
              .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

      // 상품 조회
      Product product =
          productRepository
              .findById(cmd.productId())
              .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

      // 타임딜 정책 조회 (선택적)
      TimedealPolicy timedealPolicy = null;
      if (cmd.timedealPolicyId() != null) {
        timedealPolicy =
            timedealPolicyRepository
                .findById(cmd.timedealPolicyId())
                .filter(policy -> policy.getDeletedAt() == null)
                .orElseThrow(
                    () -> new CustomException(TimedealErrorCode.TIMEDEAL_POLICY_NOT_FOUND));

        // 타임딜 정책과 상품이 일치하지 않으면 예외
        if (!timedealPolicy.getProduct().getId().equals(product.getId())) {
          throw new CustomException(TimedealErrorCode.INVALID_PRODUCT_FOR_TIMEDEAL);
        }
      }

      // 단가 및 구매 유형 결정
      int unitPrice =
          (timedealPolicy != null) ? timedealPolicy.getDiscountedPrice() : product.getPrice();
      PurchaseType purchaseType =
          (timedealPolicy != null) ? PurchaseType.TIMEDEAL : PurchaseType.NORMAL;

      // 구매 컨텍스트 생성 및 처리
      PurchaseProcessContext context =
          new PurchaseProcessContext(member, product, cmd.quantity(), unitPrice, purchaseType);

      purchaseProcessor.process(context);

    } catch (Exception e) {
      saveFailureLog(cmd, e);
      throw e;
    }
  }

  private void saveFailureLog(PurchaseCommand cmd, Exception e) {
    String requestBodyJson;
    try {
      requestBodyJson = objectMapper.writeValueAsString(cmd);
    } catch (JsonProcessingException jsonException) {
      requestBodyJson =
          String.format("{\"fallback\": \"%s\"}", cmd.toString().replace("\"", "\\\""));
    }

    failureLogRepository.save(
        PurchaseFailureLog.builder()
            .member(Member.builder().id(cmd.memberId()).build())
            .product(Product.builder().id(cmd.productId()).build())
            .reason(e.getMessage())
            .requestBody(requestBodyJson)
            .occurredAt(LocalDateTime.now())
            .build());
  }
}
