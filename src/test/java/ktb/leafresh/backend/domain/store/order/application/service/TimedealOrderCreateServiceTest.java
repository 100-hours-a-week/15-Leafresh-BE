package ktb.leafresh.backend.domain.store.order.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.store.order.application.dto.PurchaseCommand;
import ktb.leafresh.backend.domain.store.order.domain.entity.PurchaseIdempotencyKey;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimedealOrderCreateService 테스트")
class TimedealOrderCreateServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TimedealPolicyRepository timedealPolicyRepository;

    @Mock
    private PurchaseIdempotencyKeyRepository idempotencyKeyRepository;

    @Mock
    private StockRedisLuaService stockRedisLuaService;

    @Mock
    private PurchaseMessagePublisher purchaseMessagePublisher;

    @InjectMocks
    private TimedealOrderCreateService service;

    private final Long memberId = 1L;
    private final Long dealId = 10L;
    private final String idempotencyKey = "unique-key";
    private final int quantity = 2;

    private final Member member = MemberFixture.of(memberId, "user@leafresh.com", "테스터");
    private final Product product = ProductFixture.createDefaultProduct();

    @Test
    @DisplayName("타임딜 주문 성공")
    void create_success() {
        TimedealPolicy policy = TimedealPolicyFixture.createOngoingTimedeal(product);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(timedealPolicyRepository.findById(dealId)).thenReturn(Optional.of(policy));
        when(stockRedisLuaService.decreaseStock(anyString(), eq(quantity))).thenReturn(1L);

        service.create(memberId, dealId, quantity, idempotencyKey);

        verify(idempotencyKeyRepository).save(any(PurchaseIdempotencyKey.class));
        verify(purchaseMessagePublisher).publish(any(PurchaseCommand.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자")
    void create_fail_memberNotFound() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        CustomException ex = catchThrowableOfType(
                () -> service.create(memberId, dealId, quantity, idempotencyKey),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("중복 구매 요청")
    void create_fail_duplicateRequest() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        doThrow(new DataIntegrityViolationException("중복")).when(idempotencyKeyRepository)
                .save(any(PurchaseIdempotencyKey.class));

        CustomException ex = catchThrowableOfType(
                () -> service.create(memberId, dealId, quantity, idempotencyKey),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(PurchaseErrorCode.DUPLICATE_PURCHASE_REQUEST);
    }

    @Test
    @DisplayName("존재하지 않는 타임딜 정책")
    void create_fail_timedealNotFound() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(idempotencyKeyRepository.save(any())).thenReturn(mock(PurchaseIdempotencyKey.class));
        when(timedealPolicyRepository.findById(dealId)).thenReturn(Optional.empty());

        CustomException ex = catchThrowableOfType(
                () -> service.create(memberId, dealId, quantity, idempotencyKey),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(TimedealErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("구매 가능 시간이 아님")
    void create_fail_invalidTime() {
        TimedealPolicy expired = TimedealPolicyFixture.createExpiredTimedeal(product);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(idempotencyKeyRepository.save(any())).thenReturn(mock(PurchaseIdempotencyKey.class));
        when(timedealPolicyRepository.findById(dealId)).thenReturn(Optional.of(expired));

        CustomException ex = catchThrowableOfType(
                () -> service.create(memberId, dealId, quantity, idempotencyKey),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(ProductErrorCode.INVALID_STATUS);
    }

    @Test
    @DisplayName("Redis 재고 없음")
    void create_fail_outOfStock() {
        TimedealPolicy policy = TimedealPolicyFixture.createOngoingTimedeal(product);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(idempotencyKeyRepository.save(any())).thenReturn(mock(PurchaseIdempotencyKey.class));
        when(timedealPolicyRepository.findById(dealId)).thenReturn(Optional.of(policy));
        when(stockRedisLuaService.decreaseStock(anyString(), eq(quantity))).thenReturn(-2L);

        CustomException ex = catchThrowableOfType(
                () -> service.create(memberId, dealId, quantity, idempotencyKey),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(ProductErrorCode.OUT_OF_STOCK);
    }
}
