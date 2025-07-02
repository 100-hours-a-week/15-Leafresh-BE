package ktb.leafresh.backend.domain.store.order.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.store.order.application.service.model.PurchaseProcessContext;
import ktb.leafresh.backend.domain.store.order.domain.entity.ProductPurchase;
import ktb.leafresh.backend.domain.store.order.domain.entity.PurchaseProcessingLog;
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
import ktb.leafresh.backend.support.fixture.MemberFixture;
import ktb.leafresh.backend.support.fixture.ProductFixture;
import ktb.leafresh.backend.support.fixture.TimedealPolicyFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseProcessorTest {

    @Mock private ProductPurchaseRepository purchaseRepository;
    @Mock private ProductRepository productRepository;
    @Mock private TimedealPolicyRepository timedealPolicyRepository;
    @Mock private PurchaseProcessingLogRepository processingLogRepository;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks private PurchaseProcessor purchaseProcessor;

    private Member member;
    private Product product;

    @BeforeEach
    void setUp() {
        member = MemberFixture.of();
        product = ProductFixture.createDefaultProduct();
        member.updateCurrentLeafPoints(10_000);
    }

    @Test
    void process_normalPurchase_success() {
        // given
        int quantity = 2;
        int unitPrice = product.getPrice();
        PurchaseProcessContext context = new PurchaseProcessContext(
                member, product, quantity, unitPrice, PurchaseType.NORMAL
        );

        // when
        purchaseProcessor.process(context);

        // then
        assertThat(product.getStock()).isEqualTo(8); // 10 - 2
        assertThat(member.getCurrentLeafPoints()).isEqualTo(10_000 - unitPrice * quantity);
        verify(productRepository).save(product);
        verify(purchaseRepository).save(any(ProductPurchase.class));
        verify(processingLogRepository).save(any(PurchaseProcessingLog.class));
    }

//    @Test
//    void process_timedealPurchase_success() throws Exception {
//        // given
//        LocalDateTime now = LocalDateTime.now();
//        TimedealPolicy timedealPolicy = TimedealPolicyFixture.createTimedeal(
//                product, 2500, 30, 10,
//                now.minusMinutes(1),
//                now.plusMinutes(1)
//        );
//        ReflectionTestUtils.setField(timedealPolicy, "deletedAt", null);
//        product.getTimedealPolicies().add(timedealPolicy);
//        ReflectionTestUtils.setField(product, "id", 1L);
//        given(productRepository.findById(1L)).willReturn(Optional.of(product));
//
//        int quantity = 1;
//        int unitPrice = timedealPolicy.getDiscountedPrice();
//        PurchaseProcessContext context = new PurchaseProcessContext(
//                member, product, quantity, unitPrice, PurchaseType.TIMEDEAL
//        );
//
//        given(objectMapper.writeValueAsString(any())).willReturn("{\"stock\":9}");
//
//        // when
//        purchaseProcessor.process(context);
//
//        // then
//        assertThat(timedealPolicy.getStock()).isEqualTo(9);
//        assertThat(member.getCurrentLeafPoints()).isEqualTo(10_000 - unitPrice);
//        verify(timedealPolicyRepository).save(timedealPolicy);
//        verify(purchaseRepository).save(any());
//        verify(processingLogRepository).save(any());
//        verify(redisTemplate.opsForValue()).set(anyString(), eq("{\"stock\":9}"));
//    }

    @Test
    void process_withInsufficientStock_throwsException() {
        // given
        product.updateStock(1);
        int quantity = 2;
        PurchaseProcessContext context = new PurchaseProcessContext(
                member, product, quantity, product.getPrice(), PurchaseType.NORMAL
        );

        // when & then
        assertThatThrownBy(() -> purchaseProcessor.process(context))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ProductErrorCode.OUT_OF_STOCK.getMessage());
    }

    @Test
    void process_withInsufficientPoints_throwsException() {
        // given
        member.updateCurrentLeafPoints(100); // 부족한 포인트
        int quantity = 1;
        PurchaseProcessContext context = new PurchaseProcessContext(
                member, product, quantity, product.getPrice(), PurchaseType.NORMAL
        );

        // when & then
        assertThatThrownBy(() -> purchaseProcessor.process(context))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(PurchaseErrorCode.INSUFFICIENT_POINTS.getMessage());
    }

    @Test
    void process_timedealPurchaseWithoutValidPolicy_throwsException() {
        // given: 유효한 정책 없음
        TimedealPolicy expired = TimedealPolicyFixture.createExpiredTimedeal(product);
        product.getTimedealPolicies().add(expired);

        PurchaseProcessContext context = new PurchaseProcessContext(
                member, product, 1, 3000, PurchaseType.TIMEDEAL
        );

        // when & then
        assertThatThrownBy(() -> purchaseProcessor.process(context))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage());
    }
}
