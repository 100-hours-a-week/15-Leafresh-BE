package ktb.leafresh.backend.domain.store.order.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.store.order.application.dto.PurchaseCommand;
import ktb.leafresh.backend.domain.store.order.application.facade.ProductCacheLockFacade;
import ktb.leafresh.backend.domain.store.order.domain.entity.PurchaseIdempotencyKey;
import ktb.leafresh.backend.domain.store.order.infrastructure.publisher.PurchaseMessagePublisher;
import ktb.leafresh.backend.domain.store.order.infrastructure.repository.PurchaseIdempotencyKeyRepository;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.global.exception.ProductErrorCode;
import ktb.leafresh.backend.global.exception.PurchaseErrorCode;
import ktb.leafresh.backend.global.util.redis.StockRedisLuaService;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import ktb.leafresh.backend.support.fixture.ProductFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ProductOrderCreateServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ProductRepository productRepository;

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
    private ProductOrderCreateService productOrderCreateService;

    private Member member;
    private Product product;

    @BeforeEach
    void setUp() {
        member = MemberFixture.of();
        product = ProductFixture.createDefaultProduct();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("정상적인 주문 요청이면 메시지를 발행한다")
        void create_success() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(productRepository.findById(10L)).willReturn(Optional.of(product));
            given(pointService.hasEnoughPoints(eq(1L), anyInt())).willReturn(true);
            given(stockRedisLuaService.decreaseStock("stock:product:10", 1)).willReturn(1L);

            // when
            productOrderCreateService.create(1L, 10L, 1, "idempotent-key");

            // then
            verify(idempotencyRepository).save(any(PurchaseIdempotencyKey.class));
            verify(purchaseMessagePublisher).publish(any(PurchaseCommand.class));
        }

        @Test
        @DisplayName("존재하지 않는 회원이면 예외를 던진다")
        void create_memberNotFound() {
            given(memberRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    productOrderCreateService.create(1L, 10L, 1, "key"))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("Idempotency 키가 중복되면 예외를 던진다")
        void create_duplicateIdempotencyKey() {
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            willThrow(new DataIntegrityViolationException("duplicate"))
                    .given(idempotencyRepository).save(any());

            assertThatThrownBy(() ->
                    productOrderCreateService.create(1L, 10L, 1, "duplicate-key"))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(PurchaseErrorCode.DUPLICATE_PURCHASE_REQUEST.getMessage());
        }

        @Test
        @DisplayName("존재하지 않는 상품이면 예외를 던진다")
        void create_productNotFound() {
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(idempotencyRepository.save(any())).willReturn(null);
            given(productRepository.findById(10L)).willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    productOrderCreateService.create(1L, 10L, 1, "key"))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("Redis 재고가 -1이면 PRODUCT_NOT_FOUND 예외를 던진다")
        void create_redisReturnsMinusOne() {
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(productRepository.findById(10L)).willReturn(Optional.of(product));
            given(pointService.hasEnoughPoints(eq(1L), anyInt())).willReturn(true);
            given(stockRedisLuaService.decreaseStock(any(), anyInt())).willReturn(-1L);

            assertThatThrownBy(() ->
                    productOrderCreateService.create(1L, 10L, 1, "key"))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("Redis 재고가 -2이면 OUT_OF_STOCK 예외를 던진다")
        void create_redisReturnsMinusTwo() {
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(productRepository.findById(10L)).willReturn(Optional.of(product));
            given(pointService.hasEnoughPoints(eq(1L), anyInt())).willReturn(true);
            given(stockRedisLuaService.decreaseStock(any(), anyInt())).willReturn(-2L);

            assertThatThrownBy(() ->
                    productOrderCreateService.create(1L, 10L, 1, "key"))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ProductErrorCode.OUT_OF_STOCK.getMessage());
        }
    }
}
