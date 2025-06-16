package ktb.leafresh.backend.domain.store.product.application.service;

import ktb.leafresh.backend.domain.store.product.application.event.ProductUpdatedEvent;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.ProductCacheService;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.TimedealPolicyRepository;
import ktb.leafresh.backend.domain.store.product.presentation.dto.request.TimedealUpdateRequestDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.TimedealErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static ktb.leafresh.backend.support.fixture.ProductFixture.of;
import ktb.leafresh.backend.support.fixture.TimedealPolicyFixture;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimedealUpdateServiceTest {

    private TimedealPolicyRepository policyRepository;
    private ProductCacheService productCacheService;
    private ApplicationEventPublisher eventPublisher;

    private TimedealUpdateService service;

    @BeforeEach
    void setUp() {
        policyRepository = mock(TimedealPolicyRepository.class);
        productCacheService = mock(ProductCacheService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        service = new TimedealUpdateService(policyRepository, productCacheService, eventPublisher);
    }

    @Test
    @DisplayName("타임딜 수정 성공 - 재고, 시간 변경 포함")
    void updateTimedeal_success() {
        Product product = of("주방세제", 3000, 100);
        TimedealPolicy policy = TimedealPolicyFixture.of(product);

        OffsetDateTime newStart = policy.getStartTime().atOffset(ZoneOffset.UTC);
        OffsetDateTime newEnd = policy.getEndTime().atOffset(ZoneOffset.UTC);

        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policyRepository.existsByProductIdAndTimeOverlapExceptSelf(any(), any(), any(), any())).thenReturn(false);

        TimedealUpdateRequestDto dto = new TimedealUpdateRequestDto(
                newStart, newEnd, 2100, 15, 20
        );

        service.update(1L, dto);

        assertThat(policy.getStock()).isEqualTo(20);
        assertThat(policy.getDiscountedPrice()).isEqualTo(2100);
        assertThat(policy.getDiscountedPercentage()).isEqualTo(15);
        assertThat(policy.getStartTime()).isEqualTo(newStart.toLocalDateTime());
        assertThat(policy.getEndTime()).isEqualTo(newEnd.toLocalDateTime());

        verify(productCacheService).cacheTimedealStock(eq(policy.getId()), eq(20), eq(newEnd.toLocalDateTime()));
        verify(productCacheService).evictTimedealCache(eq(policy));
        verify(productCacheService).updateSingleTimedealCache(eq(policy));
        verify(eventPublisher).publishEvent(any(ProductUpdatedEvent.class));
    }

    @Test
    @DisplayName("타임딜 수정 실패 - 존재하지 않는 정책")
    void updateTimedeal_fail_notFound() {
        when(policyRepository.findById(1L)).thenReturn(Optional.empty());

        TimedealUpdateRequestDto dto = new TimedealUpdateRequestDto(null, null, null, null, 10);
        CustomException exception = catchThrowableOfType(() -> service.update(1L, dto), CustomException.class);

        assertThat(exception.getErrorCode()).isEqualTo(TimedealErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("타임딜 수정 실패 - 시간 유효성 오류")
    void updateTimedeal_fail_invalidTime() {
        Product product = of("주방세제", 3000, 100);
        TimedealPolicy policy = TimedealPolicyFixture.of(product);

        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));

        TimedealUpdateRequestDto dto = new TimedealUpdateRequestDto(
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(2), OffsetDateTime.now(ZoneOffset.UTC).minusHours(1), null, null, 10
        );

        CustomException exception = catchThrowableOfType(() -> service.update(1L, dto), CustomException.class);
        assertThat(exception.getErrorCode()).isEqualTo(TimedealErrorCode.INVALID_TIME);
    }

    @Test
    @DisplayName("타임딜 수정 실패 - 시간 중복")
    void updateTimedeal_fail_overlapTime() {
        Product product = of("주방세제", 3000, 100);
        TimedealPolicy policy = TimedealPolicyFixture.of(product);

        OffsetDateTime newStart = policy.getStartTime().atOffset(ZoneOffset.UTC);
        OffsetDateTime newEnd = policy.getEndTime().atOffset(ZoneOffset.UTC);

        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policyRepository.existsByProductIdAndTimeOverlapExceptSelf(any(), any(), any(), any()))
                .thenReturn(true);

        TimedealUpdateRequestDto dto = new TimedealUpdateRequestDto(
                newStart, newEnd, null, null, 10
        );

        CustomException exception = catchThrowableOfType(() -> service.update(1L, dto), CustomException.class);
        assertThat(exception.getErrorCode()).isEqualTo(TimedealErrorCode.OVERLAPPING_TIME);
    }

    @Test
    @DisplayName("타임딜 수정 실패 - 유효하지 않은 할인율, 가격, 재고")
    void updateTimedeal_fail_invalidDiscountAndStock() {
        Product product = of("주방세제", 3000, 100);
        TimedealPolicy policy = TimedealPolicyFixture.of(product);

        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));

        // 할인율 < 1
        var invalidPercent = new TimedealUpdateRequestDto(null, null, null, 0, 10);
        assertThatThrownBy(() -> service.update(1L, invalidPercent))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(TimedealErrorCode.INVALID_PERCENT);

        // 할인 가격 < 1
        var invalidPrice = new TimedealUpdateRequestDto(null, null, 0, null, 10);
        assertThatThrownBy(() -> service.update(1L, invalidPrice))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(TimedealErrorCode.INVALID_PRICE);

        // 재고 < 0
        var invalidStock = new TimedealUpdateRequestDto(null, null, null, null, -1);
        assertThatThrownBy(() -> service.update(1L, invalidStock))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(TimedealErrorCode.INVALID_STOCK);
    }
}
