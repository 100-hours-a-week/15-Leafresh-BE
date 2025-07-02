//package ktb.leafresh.backend.domain.store.order.application.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import ktb.leafresh.backend.domain.member.domain.entity.Member;
//import ktb.leafresh.backend.domain.store.order.application.service.model.PurchaseProcessContext;
//import ktb.leafresh.backend.domain.store.order.domain.entity.enums.PurchaseProcessingStatus;
//import ktb.leafresh.backend.domain.store.order.domain.entity.enums.PurchaseType;
//import ktb.leafresh.backend.domain.store.order.infrastructure.repository.ProductPurchaseRepository;
//import ktb.leafresh.backend.domain.store.order.infrastructure.repository.PurchaseProcessingLogRepository;
//import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
//import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;
//import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
//import ktb.leafresh.backend.domain.store.product.infrastructure.repository.TimedealPolicyRepository;
//import ktb.leafresh.backend.global.exception.CustomException;
//import ktb.leafresh.backend.global.exception.ProductErrorCode;
//import ktb.leafresh.backend.global.exception.PurchaseErrorCode;
//import ktb.leafresh.backend.support.fixture.MemberFixture;
//import ktb.leafresh.backend.support.fixture.ProductFixture;
//import ktb.leafresh.backend.support.fixture.TimedealPolicyFixture;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("PurchaseProcessor 테스트")
//class PurchaseProcessorTest {
//
//    @Mock
//    private ProductPurchaseRepository purchaseRepository;
//
//    @Mock
//    private ProductRepository productRepository;
//
//    @Mock
//    private TimedealPolicyRepository timedealPolicyRepository;
//
//    @Mock
//    private PurchaseProcessingLogRepository logRepository;
//
//    @Mock
//    private StringRedisTemplate redisTemplate;
//
//    @Mock
//    private ObjectMapper objectMapper;
//
//    @Mock
//    private ValueOperations<String, String> valueOperations;
//
//    @InjectMocks
//    private PurchaseProcessor processor;
//
//    @BeforeEach
//    void init() {
//        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
//    }
//
//    @Test
//    @DisplayName("일반 상품 구매 성공")
//    void process_normalPurchase_success() {
//        // given
//        Member member = MemberFixture.of(1L, "user@leafresh.com", "테스터");
//        member.updateCurrentLeafPoints(10_000);
//
//        Product product = ProductFixture.createDefaultProduct();
//        int quantity = 2;
//
//        PurchaseProcessContext context = new PurchaseProcessContext(
//                member, product, quantity, 3000, PurchaseType.NORMAL
//        );
//
//        // when
//        processor.process(context);
//
//        // then
//        assertThat(member.getCurrentLeafPoints()).isEqualTo(4000);
//        assertThat(product.getStock()).isEqualTo(8);
//
//        verify(productRepository).save(product);
//        verify(purchaseRepository).save(any());
//        verify(logRepository).save(argThat(log ->
//                log.getStatus() == PurchaseProcessingStatus.SUCCESS
//        ));
//    }
//
//    @Test
//    @DisplayName("타임딜 구매 성공")
//    void process_timedealPurchase_success() throws Exception {
//        // given
//        Member member = MemberFixture.of(1L, "user@leafresh.com", "테스터");
//        member.updateCurrentLeafPoints(5000);
//
//        Product product = ProductFixture.createDefaultProduct();
//        TimedealPolicy policy = TimedealPolicyFixture.createOngoingTimedeal(product);
//        product.getTimedealPolicies().add(policy);
//
//        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
//
//        PurchaseProcessContext context = new PurchaseProcessContext(
//                member, product, 1, 2500, PurchaseType.TIMEDEAL
//        );
//
//        // when
//        processor.process(context);
//
//        // then
//        assertThat(member.getCurrentLeafPoints()).isEqualTo(2500);
//        assertThat(policy.getStock()).isEqualTo(9);
//
//        verify(timedealPolicyRepository).save(policy);
//        verify(purchaseRepository).save(any());
//        verify(logRepository).save(any());
//        verify(valueOperations).set(contains("timedeal:item"), eq("{}"));
//    }
//
//    @Test
//    @DisplayName("타임딜 정책 없음 - 예외 발생")
//    void process_timedealPolicyNotFound_throws() {
//        // given
//        Member member = MemberFixture.of(1L, "user@leafresh.com", "테스터");
//        member.updateCurrentLeafPoints(10_000);
//
//        Product product = ProductFixture.createDefaultProduct(); // 타임딜 정책 없음
//
//        PurchaseProcessContext context = new PurchaseProcessContext(
//                member, product, 1, 2500, PurchaseType.TIMEDEAL
//        );
//
//        // when & then
//        assertThatThrownBy(() -> processor.process(context))
//                .isInstanceOf(CustomException.class)
//                .satisfies(e -> {
//                    CustomException ex = (CustomException) e;
//                    assertThat(ex.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
//                    assertThat(ex.getMessage()).isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage());
//                });
//    }
//
//    @Test
//    @DisplayName("포인트 부족 - 예외 발생")
//    void process_insufficientPoints_throws() {
//        // given
//        Member member = MemberFixture.of(1L, "user@leafresh.com", "테스터");
//        member.updateCurrentLeafPoints(1000);
//
//        Product product = ProductFixture.createDefaultProduct();
//
//        PurchaseProcessContext context = new PurchaseProcessContext(
//                member, product, 1, 3000, PurchaseType.NORMAL
//        );
//
//        // when & then
//        assertThatThrownBy(() -> processor.process(context))
//                .isInstanceOf(CustomException.class)
//                .satisfies(e -> {
//                    CustomException ex = (CustomException) e;
//                    assertThat(ex.getErrorCode()).isEqualTo(PurchaseErrorCode.INSUFFICIENT_POINTS);
//                    assertThat(ex.getMessage()).isEqualTo(PurchaseErrorCode.INSUFFICIENT_POINTS.getMessage());
//                });
//    }
//}
