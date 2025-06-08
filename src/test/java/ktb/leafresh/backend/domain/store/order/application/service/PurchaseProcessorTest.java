package ktb.leafresh.backend.domain.store.order.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.store.order.application.service.model.PurchaseProcessContext;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PurchaseProcessorTest {

    private ProductPurchaseRepository purchaseRepository;
    private ProductRepository productRepository;
    private TimedealPolicyRepository timedealPolicyRepository;
    private PurchaseProcessingLogRepository logRepository;

    private PurchaseProcessor processor;

    @BeforeEach
    void setUp() {
        purchaseRepository = mock(ProductPurchaseRepository.class);
        productRepository = mock(ProductRepository.class);
        timedealPolicyRepository = mock(TimedealPolicyRepository.class);
        logRepository = mock(PurchaseProcessingLogRepository.class);

        processor = new PurchaseProcessor(
                purchaseRepository,
                productRepository,
                timedealPolicyRepository,
                logRepository
        );
    }

    @Test
    @DisplayName("일반 상품 구매 성공")
    void process_normalPurchase_success() {
        // given
        Member member = MemberFixture.of(1L, "user@leafresh.com", "테스터");
        member.updateCurrentLeafPoints(10_000);

        Product product = ProductFixture.of("일반상품", 3000, 10);
        int quantity = 2;

        PurchaseProcessContext context = new PurchaseProcessContext(
                member, product, quantity, 3000, PurchaseType.NORMAL
        );

        // when
        processor.process(context);

        // then
        assertThat(member.getCurrentLeafPoints()).isEqualTo(4000);
        assertThat(product.getStock()).isEqualTo(8);

        verify(productRepository).save(product);
        verify(purchaseRepository).save(any());
        verify(logRepository).save(any());
    }

    @Test
    @DisplayName("타임딜 구매 성공")
    void process_timedealPurchase_success() {
        // given
        Member member = MemberFixture.of(1L, "user@leafresh.com", "테스터");
        member.updateCurrentLeafPoints(5000);

        Product product = ProductFixture.of("타임딜상품", 3000, 10);
        TimedealPolicy validPolicy = TimedealPolicyFixture.of(product);
        product.getTimedealPolicies().add(validPolicy);  // setter 없이 직접 추가

        PurchaseProcessContext context = new PurchaseProcessContext(
                member, product, 1, 2500, PurchaseType.TIMEDEAL
        );

        // when
        processor.process(context);

        // then
        assertThat(member.getCurrentLeafPoints()).isEqualTo(2500);
        assertThat(validPolicy.getStock()).isEqualTo(9);

        verify(timedealPolicyRepository).save(validPolicy);
        verify(purchaseRepository).save(any());
        verify(logRepository).save(any());
    }

    @Test
    @DisplayName("타임딜 정책 없음 - 예외 발생")
    void process_timedealPolicyNotFound_throws() {
        // given
        Member member = MemberFixture.of(1L, "user@leafresh.com", "테스터");
        member.updateCurrentLeafPoints(10_000);

        Product product = ProductFixture.of("타임딜상품", 3000, 10);  // 정책 없이 생성

        PurchaseProcessContext context = new PurchaseProcessContext(
                member, product, 1, 2500, PurchaseType.TIMEDEAL
        );

        // when & then
        assertThatThrownBy(() -> processor.process(context))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("포인트 부족 - 예외 발생")
    void process_insufficientPoints_throws() {
        // given
        Member member = MemberFixture.of(1L, "user@leafresh.com", "테스터");
        member.updateCurrentLeafPoints(1000);

        Product product = ProductFixture.of("일반상품", 3000, 10);

        PurchaseProcessContext context = new PurchaseProcessContext(
                member, product, 1, 3000, PurchaseType.NORMAL
        );

        // when & then
        assertThatThrownBy(() -> processor.process(context))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(PurchaseErrorCode.INSUFFICIENT_POINTS);
    }
}
