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
import java.util.Optional;

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
            Member member = memberRepository.findById(cmd.memberId())
                    .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

            Product product = productRepository.findById(cmd.productId())
                    .orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

            Optional<TimedealPolicy> timedealOpt = Optional.empty();
            if (cmd.timedealPolicyId() != null) {
                timedealOpt = timedealPolicyRepository.findById(cmd.timedealPolicyId())
                        .filter(policy -> policy.getDeletedAt() == null);
            } else {
                timedealOpt = product.getPurchasableTimedealPolicy(LocalDateTime.now());
            }

            int unitPrice = timedealOpt.map(TimedealPolicy::getDiscountedPrice).orElse(product.getPrice());
            PurchaseType purchaseType = timedealOpt.isPresent() ? PurchaseType.TIMEDEAL : PurchaseType.NORMAL;

            PurchaseProcessContext context = new PurchaseProcessContext(
                    member, product, cmd.quantity(), unitPrice, purchaseType
            );

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
            requestBodyJson = String.format("{\"fallback\": \"%s\"}", cmd.toString().replace("\"", "\\\""));
        }

        failureLogRepository.save(PurchaseFailureLog.builder()
                .member(Member.builder().id(cmd.memberId()).build())
                .product(Product.builder().id(cmd.productId()).build())
                .reason(e.getMessage())
                .requestBody(requestBodyJson)
                .occurredAt(LocalDateTime.now())
                .build());
    }
}
