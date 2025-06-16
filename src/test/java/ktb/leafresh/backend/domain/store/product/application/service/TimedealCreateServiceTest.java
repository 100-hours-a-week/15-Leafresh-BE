package ktb.leafresh.backend.domain.store.product.application.service;

import ktb.leafresh.backend.domain.store.product.application.event.ProductUpdatedEvent;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.ProductCacheService;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.TimedealPolicyRepository;
import ktb.leafresh.backend.domain.store.product.presentation.dto.request.TimedealCreateRequestDto;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.TimedealCreateResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.TimedealErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static ktb.leafresh.backend.support.fixture.ProductFixture.of;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimedealCreateServiceTest {

    private ProductRepository productRepository;
    private TimedealPolicyRepository timedealPolicyRepository;
    private ProductCacheService productCacheService;
    private ApplicationEventPublisher eventPublisher;

    private TimedealCreateService service;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        timedealPolicyRepository = mock(TimedealPolicyRepository.class);
        productCacheService = mock(ProductCacheService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        service = new TimedealCreateService(
                productRepository,
                timedealPolicyRepository,
                eventPublisher,
                productCacheService
        );
    }

    @Test
    @DisplayName("타임딜 생성 성공")
    void createTimedeal_success() {
        // given
        Product product = of("친환경 주방세제", 3000, 50);
        OffsetDateTime start = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        OffsetDateTime end = OffsetDateTime.now(ZoneOffset.UTC).plusHours(2);

        TimedealCreateRequestDto dto = new TimedealCreateRequestDto(
                product.getId(),
                start,
                end,
                2000,
                33
        );

        when(productRepository.findById(dto.productId())).thenReturn(Optional.of(product));
        when(timedealPolicyRepository.existsByProductIdAndTimeOverlap(anyLong(), any(), any())).thenReturn(false);
        when(timedealPolicyRepository.save(any(TimedealPolicy.class))).thenAnswer(invocation -> {
            TimedealPolicy saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 10L);
            return saved;
        });
        when(productRepository.findById(dto.productId())).thenReturn(Optional.of(product)); // 캐시 갱신용

        // when
        TimedealCreateResponseDto response = service.create(dto);

        // then
        assertThat(response.dealId()).isEqualTo(10L);
        verify(productCacheService).cacheTimedealStock(eq(10L), eq(50), eq(end.toLocalDateTime()));
        verify(productCacheService).updateSingleTimedealCache(any(TimedealPolicy.class));
        verify(eventPublisher).publishEvent(any(ProductUpdatedEvent.class));
    }

    @Test
    @DisplayName("타임딜 생성 실패 - 상품 없음")
    void createTimedeal_fail_productNotFound() {
        TimedealCreateRequestDto dto = new TimedealCreateRequestDto(
                999L,
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(1),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(2),
                2000,
                20
        );
        when(productRepository.findById(dto.productId())).thenReturn(Optional.empty());

        CustomException exception = catchThrowableOfType(() -> service.create(dto), CustomException.class);
        assertThat(exception.getErrorCode()).isEqualTo(TimedealErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("타임딜 생성 실패 - 시작 시간이 종료 시간보다 이후")
    void createTimedeal_fail_invalidTime() {
        Product product = of("친환경 주방세제", 3000, 50);
        TimedealCreateRequestDto dto = new TimedealCreateRequestDto(
                product.getId(),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(3),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(1),
                2000,
                20
        );
        when(productRepository.findById(dto.productId())).thenReturn(Optional.of(product));

        CustomException exception = catchThrowableOfType(() -> service.create(dto), CustomException.class);
        assertThat(exception.getErrorCode()).isEqualTo(TimedealErrorCode.INVALID_TIME);
    }

    @Test
    @DisplayName("타임딜 생성 실패 - 타임딜 시간 중복")
    void createTimedeal_fail_overlap() {
        Product product = of("친환경 주방세제", 3000, 50);
        TimedealCreateRequestDto dto = new TimedealCreateRequestDto(
                product.getId(),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(1),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(2),
                2000,
                20
        );

        when(productRepository.findById(dto.productId())).thenReturn(Optional.of(product));
        when(timedealPolicyRepository.existsByProductIdAndTimeOverlap(anyLong(), any(), any())).thenReturn(true);

        CustomException exception = catchThrowableOfType(() -> service.create(dto), CustomException.class);
        assertThat(exception.getErrorCode()).isEqualTo(TimedealErrorCode.OVERLAPPING_TIME);
    }

    @Test
    @DisplayName("타임딜 생성 실패 - 저장 중 예외 발생")
    void createTimedeal_fail_saveException() {
        Product product = of("친환경 주방세제", 3000, 50);
        TimedealCreateRequestDto dto = new TimedealCreateRequestDto(
                product.getId(),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(1),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(2),
                2000,
                20
        );

        when(productRepository.findById(dto.productId())).thenReturn(Optional.of(product));
        when(timedealPolicyRepository.existsByProductIdAndTimeOverlap(anyLong(), any(), any())).thenReturn(false);
        when(timedealPolicyRepository.save(any())).thenThrow(new RuntimeException("DB 실패"));

        CustomException exception = catchThrowableOfType(() -> service.create(dto), CustomException.class);
        assertThat(exception.getErrorCode()).isEqualTo(TimedealErrorCode.TIMEDEAL_SAVE_FAIL);
    }
}
