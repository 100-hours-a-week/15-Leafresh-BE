package ktb.leafresh.backend.domain.store.order.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.store.order.application.dto.PurchaseCommand;
import ktb.leafresh.backend.domain.store.order.application.facade.ProductCacheLockFacade;
import ktb.leafresh.backend.domain.store.order.infrastructure.publisher.PurchaseMessagePublisher;
import ktb.leafresh.backend.domain.store.order.infrastructure.repository.PurchaseIdempotencyKeyRepository;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.TimedealPolicyRepository;
import ktb.leafresh.backend.global.exception.*;
import ktb.leafresh.backend.global.util.redis.StockRedisLuaService;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import ktb.leafresh.backend.support.fixture.ProductFixture;
import ktb.leafresh.backend.support.fixture.TimedealPolicyFixture;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class TimedealOrderCreateServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TimedealPolicyRepository timedealPolicyRepository;

    @Mock
    private PurchaseIdempotencyKeyRepository idempotencyRepository;

    @Mock
    private StockRedisLuaService stockRedisLuaService;

    @Mock
    private PurchaseMessagePublisher purchaseMessagePublisher;

    @Mock
    private ProductCacheLockFacade productCacheLockFacade;

    @Mock
    private PointService pointService;

    @InjectMocks
    private TimedealOrderCreateService service;

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2025, 7, 1, 12, 0);
    private static MockedStatic<LocalDateTime> localDateTimeMock;

    private Member member;
    private Product product;
    private TimedealPolicy policy;

    @BeforeAll
    static void beforeAll() {
        localDateTimeMock = Mockito.mockStatic(LocalDateTime.class, CALLS_REAL_METHODS);
        localDateTimeMock.when(LocalDateTime::now).thenReturn(FIXED_NOW);
    }

    @AfterAll
    static void afterAll() {
        localDateTimeMock.close();
    }

    @BeforeEach
    void setUp() {
        member = MemberFixture.of();
        product = ProductFixture.createDefaultProduct();
        policy = TimedealPolicyFixture.createDefaultTimedeal(product);
    }

    @Test
    void createTimedealOrder_withValidInput_succeeds() {
        // given
        Long memberId = 1L;
        Long dealId = 2L;
        int quantity = 1;
        String idempotencyKey = "unique-key";

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(timedealPolicyRepository.findById(dealId)).willReturn(Optional.of(policy));
        given(pointService.hasEnoughPoints(eq(memberId), anyInt())).willReturn(true);
        given(stockRedisLuaService.decreaseStock(anyString(), eq(quantity))).willReturn(1L);
        willDoNothing().given(productCacheLockFacade).updateSingleTimedealCache(policy);

        // when & then
        assertThatCode(() -> service.create(memberId, dealId, quantity, idempotencyKey))
                .doesNotThrowAnyException();

        then(idempotencyRepository).should().save(any());
        then(purchaseMessagePublisher).should().publish(any(PurchaseCommand.class));
    }

    @Test
    void createTimedealOrder_withDuplicateIdempotencyKey_throwsException() {
        // given
        Long memberId = 1L;
        Long dealId = 2L;
        String key = "duplicate-key";

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        willThrow(DataIntegrityViolationException.class).given(idempotencyRepository).save(any());

        // when & then
        assertThatThrownBy(() -> service.create(memberId, dealId, 1, key))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(PurchaseErrorCode.DUPLICATE_PURCHASE_REQUEST.getMessage());
    }

    @Test
    void createTimedealOrder_withExpiredPolicy_throwsException() {
        // given
        TimedealPolicy expiredPolicy = TimedealPolicyFixture.createExpiredTimedeal(product);

        given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));
        given(idempotencyRepository.save(any())).willReturn(null);
        given(timedealPolicyRepository.findById(anyLong())).willReturn(Optional.of(expiredPolicy));

        // when & then
        assertThatThrownBy(() -> service.create(1L, 2L, 1, "key"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("현재는 구매할 수 없는 시간입니다.");
    }

    @Test
    void createTimedealOrder_withOutOfStock_throwsException() {
        // given
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));
        given(idempotencyRepository.save(any())).willReturn(null);
        given(timedealPolicyRepository.findById(anyLong())).willReturn(Optional.of(policy));
        given(pointService.hasEnoughPoints(eq(1L), anyInt())).willReturn(true);
        given(stockRedisLuaService.decreaseStock(anyString(), anyInt())).willReturn(-2L);

        // when & then
        assertThatThrownBy(() -> service.create(1L, 2L, 1, "key"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ProductErrorCode.OUT_OF_STOCK.getMessage());
    }

    @Test
    void createTimedealOrder_withMissingMember_throwsException() {
        // given
        given(memberRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.create(1L, 2L, 1, "key"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    void createTimedealOrder_withInvalidPolicyId_throwsException() {
        // given
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));
        given(idempotencyRepository.save(any())).willReturn(null);
        given(timedealPolicyRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.create(1L, 2L, 1, "key"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(TimedealErrorCode.PRODUCT_NOT_FOUND.getMessage());
    }

    @Test
    void createTimedealOrder_withMissingStockInRedis_throwsException() {
        // given
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));
        given(idempotencyRepository.save(any())).willReturn(null);
        given(timedealPolicyRepository.findById(anyLong())).willReturn(Optional.of(policy));
        given(pointService.hasEnoughPoints(eq(1L), anyInt())).willReturn(true);
        given(stockRedisLuaService.decreaseStock(anyString(), anyInt())).willReturn(-1L);

        // when & then
        assertThatThrownBy(() -> service.create(1L, 2L, 1, "key"))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage());
    }
}
