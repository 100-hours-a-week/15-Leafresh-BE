package ktb.leafresh.backend.domain.store.product.application.service;

import ktb.leafresh.backend.domain.store.order.application.facade.ProductCacheLockFacade;
import ktb.leafresh.backend.domain.store.product.application.event.ProductUpdatedEvent;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.TimedealPolicyRepository;
import ktb.leafresh.backend.domain.store.product.presentation.dto.request.TimedealUpdateRequestDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.TimedealErrorCode;
import ktb.leafresh.backend.support.fixture.ProductFixture;
import ktb.leafresh.backend.support.fixture.TimedealPolicyFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimedealUpdateService 테스트")
class TimedealUpdateServiceTest {

    @Mock
    private TimedealPolicyRepository policyRepository;

    @Mock
    private ProductCacheLockFacade productCacheLockFacade;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TimedealUpdateService service;

    private static final OffsetDateTime FIXED_NOW = OffsetDateTime.of(2025, 7, 1, 12, 0, 0, 0, ZoneOffset.UTC);

    @Test
    @DisplayName("타임딜 수정 성공 - 재고, 시간 변경 포함")
    void updateTimedeal_success() {
        Product product = ProductFixture.createActiveProduct("주방세제", 3000, 100);
        TimedealPolicy policy = TimedealPolicyFixture.createDefaultTimedeal(product);

        OffsetDateTime newStart = FIXED_NOW.plusHours(1);
        OffsetDateTime newEnd = FIXED_NOW.plusHours(2);

        TimedealUpdateRequestDto dto = new TimedealUpdateRequestDto(newStart, newEnd, 2100, 15, 20);

        when(policyRepository.findById(policy.getId())).thenReturn(Optional.of(policy));
        when(policyRepository.existsByProductIdAndTimeOverlapExceptSelf(
                product.getId(), newStart.toLocalDateTime(), newEnd.toLocalDateTime(), policy.getId())
        ).thenReturn(false);

        service.update(policy.getId(), dto);

        assertThat(policy.getDiscountedPrice()).isEqualTo(dto.discountedPrice());
        assertThat(policy.getDiscountedPercentage()).isEqualTo(dto.discountedPercentage());
        assertThat(policy.getStock()).isEqualTo(dto.stock());
        assertThat(policy.getStartTime()).isEqualTo(dto.startTime().toLocalDateTime());
        assertThat(policy.getEndTime()).isEqualTo(dto.endTime().toLocalDateTime());

        verify(productCacheLockFacade).cacheTimedealStock(policy.getId(), dto.stock(), dto.endTime().toLocalDateTime());
        verify(productCacheLockFacade).evictTimedealCache(policy);
        verify(productCacheLockFacade).updateSingleTimedealCache(policy);
        verify(eventPublisher).publishEvent(any(ProductUpdatedEvent.class));
    }

    @Test
    @DisplayName("타임딜 수정 실패 - 존재하지 않는 정책")
    void updateTimedeal_fail_notFound() {
        Long invalidId = 999L;
        TimedealUpdateRequestDto dto = new TimedealUpdateRequestDto(null, null, null, null, 10);
        when(policyRepository.findById(invalidId)).thenReturn(Optional.empty());

        CustomException exception = catchThrowableOfType(() -> service.update(invalidId, dto), CustomException.class);

        assertThat(exception.getErrorCode()).isEqualTo(TimedealErrorCode.PRODUCT_NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo(TimedealErrorCode.PRODUCT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("타임딜 수정 실패 - 시작 시간이 종료 시간보다 늦음")
    void updateTimedeal_fail_invalidTime() {
        Product product = ProductFixture.createDefaultProduct();
        TimedealPolicy policy = TimedealPolicyFixture.createDefaultTimedeal(product);

        OffsetDateTime start = FIXED_NOW.plusHours(2);
        OffsetDateTime end = FIXED_NOW.plusHours(1);

        TimedealUpdateRequestDto dto = new TimedealUpdateRequestDto(start, end, null, null, 10);

        when(policyRepository.findById(policy.getId())).thenReturn(Optional.of(policy));

        CustomException exception = catchThrowableOfType(() -> service.update(policy.getId(), dto), CustomException.class);

        assertThat(exception.getErrorCode()).isEqualTo(TimedealErrorCode.INVALID_TIME);
        assertThat(exception.getMessage()).isEqualTo(TimedealErrorCode.INVALID_TIME.getMessage());
    }

    @Test
    @DisplayName("타임딜 수정 실패 - 시간 중복")
    void updateTimedeal_fail_overlapTime() {
        Product product = ProductFixture.createDefaultProduct();
        TimedealPolicy policy = TimedealPolicyFixture.createDefaultTimedeal(product);

        OffsetDateTime start = FIXED_NOW.plusHours(1);
        OffsetDateTime end = FIXED_NOW.plusHours(2);

        TimedealUpdateRequestDto dto = new TimedealUpdateRequestDto(start, end, null, null, 10);

        when(policyRepository.findById(policy.getId())).thenReturn(Optional.of(policy));
        when(policyRepository.existsByProductIdAndTimeOverlapExceptSelf(
                product.getId(), start.toLocalDateTime(), end.toLocalDateTime(), policy.getId())
        ).thenReturn(true);

        CustomException exception = catchThrowableOfType(() -> service.update(policy.getId(), dto), CustomException.class);

        assertThat(exception.getErrorCode()).isEqualTo(TimedealErrorCode.OVERLAPPING_TIME);
        assertThat(exception.getMessage()).isEqualTo(TimedealErrorCode.OVERLAPPING_TIME.getMessage());
    }

    @Test
    @DisplayName("타임딜 수정 실패 - 할인율, 가격, 재고 유효성 실패")
    void updateTimedeal_fail_invalidFields() {
        Product product = ProductFixture.createDefaultProduct();
        TimedealPolicy policy = TimedealPolicyFixture.createDefaultTimedeal(product);

        when(policyRepository.findById(policy.getId())).thenReturn(Optional.of(policy));

        TimedealUpdateRequestDto dto1 = new TimedealUpdateRequestDto(null, null, null, 0, 10);
        CustomException ex1 = catchThrowableOfType(() -> service.update(policy.getId(), dto1), CustomException.class);
        assertThat(ex1.getErrorCode()).isEqualTo(TimedealErrorCode.INVALID_PERCENT);
        assertThat(ex1.getMessage()).isEqualTo(TimedealErrorCode.INVALID_PERCENT.getMessage());

        TimedealUpdateRequestDto dto2 = new TimedealUpdateRequestDto(null, null, 0, null, 10);
        CustomException ex2 = catchThrowableOfType(() -> service.update(policy.getId(), dto2), CustomException.class);
        assertThat(ex2.getErrorCode()).isEqualTo(TimedealErrorCode.INVALID_PRICE);
        assertThat(ex2.getMessage()).isEqualTo(TimedealErrorCode.INVALID_PRICE.getMessage());

        TimedealUpdateRequestDto dto3 = new TimedealUpdateRequestDto(null, null, null, null, -1);
        CustomException ex3 = catchThrowableOfType(() -> service.update(policy.getId(), dto3), CustomException.class);
        assertThat(ex3.getErrorCode()).isEqualTo(TimedealErrorCode.INVALID_STOCK);
        assertThat(ex3.getMessage()).isEqualTo(TimedealErrorCode.INVALID_STOCK.getMessage());
    }
}
