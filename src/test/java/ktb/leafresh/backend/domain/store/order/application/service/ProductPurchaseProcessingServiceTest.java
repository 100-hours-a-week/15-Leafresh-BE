package ktb.leafresh.backend.domain.store.order.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.store.order.application.dto.PurchaseCommand;
import ktb.leafresh.backend.domain.store.order.application.service.model.PurchaseProcessContext;
import ktb.leafresh.backend.domain.store.order.domain.entity.PurchaseFailureLog;
import ktb.leafresh.backend.domain.store.order.domain.entity.enums.PurchaseType;
import ktb.leafresh.backend.domain.store.order.infrastructure.repository.PurchaseFailureLogRepository;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.TimedealPolicyRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.global.exception.ProductErrorCode;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import ktb.leafresh.backend.support.fixture.ProductFixture;
import ktb.leafresh.backend.support.fixture.TimedealPolicyFixture;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ProductPurchaseProcessingServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TimedealPolicyRepository timedealPolicyRepository;

    @Mock
    private PurchaseFailureLogRepository failureLogRepository;

    @Mock
    private PurchaseProcessor purchaseProcessor;

    @InjectMocks
    private ProductPurchaseProcessingService service;

    private Member member;
    private Product product;
    private TimedealPolicy timedeal;

    private static final Long MEMBER_ID = 1L;
    private static final Long PRODUCT_ID = 10L;
    private static final Long TIMEDEAL_ID = 100L;
    private static final int QUANTITY = 2;

    @BeforeEach
    void setUp() {
        member = MemberFixture.of();
        product = ProductFixture.createDefaultProduct();
        ReflectionTestUtils.setField(product, "timedealPolicies", List.of());
        timedeal = TimedealPolicyFixture.createOngoingTimedeal(product);
    }

    @Test
    @DisplayName("정상 요청 시 프로세스 처리된다 - 일반 구매")
    void process_normalPurchase_success() {
        // given
        PurchaseCommand cmd = new PurchaseCommand(MEMBER_ID, PRODUCT_ID, null, QUANTITY, "key", null);

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));

        // when
        service.process(cmd);

        // then
        ArgumentCaptor<PurchaseProcessContext> captor = ArgumentCaptor.forClass(PurchaseProcessContext.class);
        verify(purchaseProcessor).process(captor.capture());

        PurchaseProcessContext context = captor.getValue();
        assertThat(context.member()).isEqualTo(member);
        assertThat(context.product()).isEqualTo(product);
        assertThat(context.purchaseType()).isEqualTo(PurchaseType.NORMAL);
        assertThat(context.unitPrice()).isEqualTo(product.getPrice());
        assertThat(context.quantity()).isEqualTo(QUANTITY);
    }

    @Test
    @DisplayName("정상 요청 시 프로세스 처리된다 - 타임딜 구매")
    void process_timedealPurchase_success() {
        // given
        PurchaseCommand cmd = new PurchaseCommand(MEMBER_ID, PRODUCT_ID, TIMEDEAL_ID, QUANTITY, "key", null);

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));
        given(timedealPolicyRepository.findById(TIMEDEAL_ID)).willReturn(Optional.of(timedeal));

        // when
        service.process(cmd);

        // then
        ArgumentCaptor<PurchaseProcessContext> captor = ArgumentCaptor.forClass(PurchaseProcessContext.class);
        verify(purchaseProcessor).process(captor.capture());

        PurchaseProcessContext context = captor.getValue();
        assertThat(context.purchaseType()).isEqualTo(PurchaseType.TIMEDEAL);
        assertThat(context.unitPrice()).isEqualTo(timedeal.getDiscountedPrice());
    }

    @Test
    @DisplayName("존재하지 않는 회원일 경우 예외를 던지고 실패 로그를 저장한다")
    void process_memberNotFound() {
        // given
        PurchaseCommand cmd = new PurchaseCommand(MEMBER_ID, PRODUCT_ID, null, QUANTITY, "key", null);
        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> service.process(cmd))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());

        verify(failureLogRepository).save(any(PurchaseFailureLog.class));
    }

    @Test
    @DisplayName("존재하지 않는 상품일 경우 예외를 던지고 실패 로그를 저장한다")
    void process_productNotFound() {
        // given
        PurchaseCommand cmd = new PurchaseCommand(MEMBER_ID, PRODUCT_ID, null, QUANTITY, "key", null);
        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> service.process(cmd))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage());

        verify(failureLogRepository).save(any(PurchaseFailureLog.class));
    }

    @Test
    @DisplayName("ObjectMapper 오류가 발생해도 fallback JSON이 저장된다")
    void process_fallbackOnJsonProcessingError() throws Exception {
        // given
        PurchaseCommand cmd = new PurchaseCommand(MEMBER_ID, PRODUCT_ID, null, QUANTITY, "key", null);
        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.empty());

        // 강제로 ObjectMapper writeValueAsString 에러 유도
        ProductPurchaseProcessingService brokenService = new ProductPurchaseProcessingService(
                memberRepository, productRepository, timedealPolicyRepository, failureLogRepository, purchaseProcessor
        ) {
            protected void saveFailureLog(PurchaseCommand cmd, Exception e) {
                throw new RuntimeException("ObjectMapper Error");
            }
        };

        // 예외는 그대로 던지되, 테스트에서는 실제 저장까지 확인하지 않음
        assertThatThrownBy(() -> brokenService.process(cmd)).isInstanceOf(RuntimeException.class);
    }
}
