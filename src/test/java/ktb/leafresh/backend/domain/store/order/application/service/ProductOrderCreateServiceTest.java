package ktb.leafresh.backend.domain.store.order.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.store.order.application.dto.PurchaseCommand;
import ktb.leafresh.backend.domain.store.order.domain.entity.PurchaseIdempotencyKey;
import ktb.leafresh.backend.domain.store.order.infrastructure.publisher.PurchaseMessagePublisher;
import ktb.leafresh.backend.domain.store.order.infrastructure.repository.PurchaseIdempotencyKeyRepository;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.global.exception.ProductErrorCode;
import ktb.leafresh.backend.global.exception.PurchaseErrorCode;
import ktb.leafresh.backend.global.util.redis.RedisLuaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static ktb.leafresh.backend.support.fixture.MemberFixture.of;
import static ktb.leafresh.backend.support.fixture.ProductFixture.of;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductOrderCreateServiceTest {

    private ProductOrderCreateService service;
    private ProductRepository productRepository;
    private PurchaseIdempotencyKeyRepository idempotencyRepository;
    private RedisLuaService redisLuaService;
    private PurchaseMessagePublisher purchaseMessagePublisher;
    private ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        idempotencyRepository = mock(PurchaseIdempotencyKeyRepository.class);
        redisLuaService = mock(RedisLuaService.class);
        purchaseMessagePublisher = mock(PurchaseMessagePublisher.class);
        memberRepository = mock(ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository.class);

        service = new ProductOrderCreateService(
                memberRepository,
                productRepository,
                idempotencyRepository,
                redisLuaService,
                purchaseMessagePublisher
        );
    }

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_success() {
        Member member = of();
        Product product = of("비누", 3000, 50);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(redisLuaService.decreaseStock(any(), anyInt())).thenReturn(1L);

        service.create(1L, 1L, 2, "unique-key");

        verify(idempotencyRepository).save(any(PurchaseIdempotencyKey.class));
        verify(purchaseMessagePublisher).publish(any(PurchaseCommand.class));
    }

    @Test
    @DisplayName("주문 실패 - 존재하지 않는 사용자")
    void createOrder_fail_memberNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        CustomException ex = catchThrowableOfType(
                () -> service.create(1L, 1L, 1, "unique-key"),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("주문 실패 - Idempotency 중복 요청")
    void createOrder_fail_idempotencyKeyDuplicate() {
        Member member = of();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        doThrow(new DataIntegrityViolationException("중복")).when(idempotencyRepository).save(any());

        CustomException ex = catchThrowableOfType(
                () -> service.create(1L, 1L, 1, "duplicate-key"),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(PurchaseErrorCode.DUPLICATE_PURCHASE_REQUEST);
    }

    @Test
    @DisplayName("주문 실패 - 상품 없음")
    void createOrder_fail_productNotFound() {
        Member member = of();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        CustomException ex = catchThrowableOfType(
                () -> service.create(1L, 1L, 1, "unique-key"),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("주문 실패 - Redis 재고 없음")
    void createOrder_fail_outOfStock() {
        Member member = of();
        Product product = of("샴푸", 5000, 0);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(redisLuaService.decreaseStock(any(), anyInt())).thenReturn(-2L);

        CustomException ex = catchThrowableOfType(
                () -> service.create(1L, 1L, 1, "unique-key"),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(ProductErrorCode.OUT_OF_STOCK);
    }
}
