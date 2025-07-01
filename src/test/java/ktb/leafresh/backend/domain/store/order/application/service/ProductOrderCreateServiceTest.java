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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductOrderCreateServiceTest {

    private static final String IDEMPOTENCY_KEY = "unique-key";
    private static final String DUPLICATE_KEY = "duplicate-key";

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PurchaseIdempotencyKeyRepository idempotencyRepository;

    @Mock
    private StockRedisLuaService stockRedisLuaService;

    @Mock
    private PurchaseMessagePublisher purchaseMessagePublisher;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ProductCacheLockFacade productCacheLockFacade;

    @InjectMocks
    private ProductOrderCreateService service;

    private Member member;
    private Product product;

    @BeforeEach
    void setUp() {
        member = MemberFixture.of(1L, "tester@leafresh.com", "테스터");
        product = ProductFixture.createActiveProduct("비누", 3000, 50);
    }

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_success() {
        // given
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(stockRedisLuaService.decreaseStock(any(), eq(2))).thenReturn(1L);

        // when
        service.create(member.getId(), product.getId(), 2, IDEMPOTENCY_KEY);

        // then
        verify(idempotencyRepository).save(any(PurchaseIdempotencyKey.class));
        verify(purchaseMessagePublisher).publish(any(PurchaseCommand.class));
    }

    @Test
    @DisplayName("주문 실패 - 존재하지 않는 사용자")
    void createOrder_fail_memberNotFound() {
        when(memberRepository.findById(member.getId())).thenReturn(Optional.empty());

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.create(member.getId(), product.getId(), 1, IDEMPOTENCY_KEY),
                CustomException.class
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("주문 실패 - Idempotency 중복 요청")
    void createOrder_fail_idempotencyKeyDuplicate() {
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        doThrow(DataIntegrityViolationException.class).when(idempotencyRepository).save(any());

        CustomException ex = catchThrowableOfType(
                () -> service.create(member.getId(), product.getId(), 1, DUPLICATE_KEY),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(PurchaseErrorCode.DUPLICATE_PURCHASE_REQUEST);
    }

    @Test
    @DisplayName("주문 실패 - 상품 없음")
    void createOrder_fail_productNotFound() {
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(productRepository.findById(product.getId())).thenReturn(Optional.empty());

        CustomException ex = catchThrowableOfType(
                () -> service.create(member.getId(), product.getId(), 1, IDEMPOTENCY_KEY),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("주문 실패 - Redis 재고 없음")
    void createOrder_fail_outOfStock() {
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(stockRedisLuaService.decreaseStock(any(), anyInt())).thenReturn(-2L);

        CustomException ex = catchThrowableOfType(
                () -> service.create(member.getId(), product.getId(), 1, IDEMPOTENCY_KEY),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(ProductErrorCode.OUT_OF_STOCK);
    }
}
