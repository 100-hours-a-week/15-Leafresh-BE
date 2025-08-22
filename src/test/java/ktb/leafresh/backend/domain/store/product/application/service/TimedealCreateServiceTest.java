package ktb.leafresh.backend.domain.store.product.application.service;

import ktb.leafresh.backend.domain.store.order.application.facade.ProductCacheLockFacade;
import ktb.leafresh.backend.domain.store.product.application.event.ProductUpdatedEvent;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.TimedealPolicyRepository;
import ktb.leafresh.backend.domain.store.product.presentation.dto.request.TimedealCreateRequestDto;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.TimedealCreateResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.TimedealErrorCode;
import ktb.leafresh.backend.support.fixture.ProductFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimedealCreateService 테스트")
class TimedealCreateServiceTest {

  @Mock private ProductRepository productRepository;

  @Mock private TimedealPolicyRepository timedealPolicyRepository;

  @Mock private ProductCacheLockFacade productCacheLockFacade;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private TimedealCreateService service;

  @Captor private ArgumentCaptor<ProductUpdatedEvent> eventCaptor;

  private static final OffsetDateTime FIXED_START =
      OffsetDateTime.of(2025, 7, 1, 10, 0, 0, 0, ZoneOffset.UTC);
  private static final OffsetDateTime FIXED_END = FIXED_START.plusHours(1);
  private static final String DB_ERROR_MESSAGE = "DB 오류";

  @Test
  @DisplayName("타임딜 생성 성공")
  void createTimedeal_success() {
    // given
    Product product = ProductFixture.createDefaultProduct();
    ReflectionTestUtils.setField(product, "id", 1L);
    int stock = 1000;

    TimedealCreateRequestDto dto =
        new TimedealCreateRequestDto(product.getId(), FIXED_START, FIXED_END, 2000, 33, stock);

    given(productRepository.findById(product.getId())).willReturn(Optional.of(product));
    given(timedealPolicyRepository.existsByProductIdAndTimeOverlap(anyLong(), any(), any()))
        .willReturn(false);
    given(timedealPolicyRepository.save(any()))
        .willAnswer(
            invocation -> {
              TimedealPolicy saved = invocation.getArgument(0);
              ReflectionTestUtils.setField(saved, "id", 10L);
              return saved;
            });

    // when
    TimedealCreateResponseDto response = service.create(dto);

    // then
    assertThat(response.dealId()).isEqualTo(10L);
    verify(productCacheLockFacade)
        .cacheTimedealStock(eq(10L), eq(stock), eq(FIXED_END.toLocalDateTime()));
    verify(productCacheLockFacade).updateSingleTimedealCache(any());
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    ProductUpdatedEvent event = eventCaptor.getValue();
    assertThat(event.productId()).isEqualTo(product.getId());
    assertThat(event.isTimeDeal()).isTrue();
  }

  @Test
  @DisplayName("타임딜 생성 실패 - 상품 없음")
  void createTimedeal_fail_productNotFound() {
    // given
    Long invalidProductId = 999L;
    TimedealCreateRequestDto dto =
        new TimedealCreateRequestDto(invalidProductId, FIXED_START, FIXED_END, 2000, 20, 100);
    given(productRepository.findById(invalidProductId)).willReturn(Optional.empty());

    // when
    CustomException exception =
        catchThrowableOfType(() -> service.create(dto), CustomException.class);

    // then
    assertThat(exception.getErrorCode()).isEqualTo(TimedealErrorCode.PRODUCT_NOT_FOUND);
    assertThat(exception.getMessage()).isEqualTo(TimedealErrorCode.PRODUCT_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("타임딜 생성 실패 - 시작 시간이 종료 시간보다 이후")
  void createTimedeal_fail_invalidTime() {
    Product product = ProductFixture.createDefaultProduct();
    ReflectionTestUtils.setField(product, "id", 1L);
    Long productId = product.getId();

    TimedealCreateRequestDto dto =
        new TimedealCreateRequestDto(productId, FIXED_END, FIXED_START, 2000, 20, 500);
    when(productRepository.findById(productId)).thenReturn(Optional.of(product));

    CustomException exception =
        catchThrowableOfType(() -> service.create(dto), CustomException.class);

    assertThat(exception.getErrorCode()).isEqualTo(TimedealErrorCode.INVALID_TIME);
    assertThat(exception.getMessage()).isEqualTo(TimedealErrorCode.INVALID_TIME.getMessage());
  }

  @Test
  @DisplayName("타임딜 생성 실패 - 타임딜 시간 중복")
  void createTimedeal_fail_overlap() {
    // given
    Product product = ProductFixture.createDefaultProduct();
    ReflectionTestUtils.setField(product, "id", 1L);

    TimedealCreateRequestDto dto =
        new TimedealCreateRequestDto(product.getId(), FIXED_START, FIXED_END, 2000, 20, 300);
    given(productRepository.findById(product.getId())).willReturn(Optional.of(product));
    given(timedealPolicyRepository.existsByProductIdAndTimeOverlap(anyLong(), any(), any()))
        .willReturn(true);

    // when
    CustomException exception =
        catchThrowableOfType(() -> service.create(dto), CustomException.class);

    // then
    assertThat(exception.getErrorCode()).isEqualTo(TimedealErrorCode.OVERLAPPING_TIME);
  }

  @Test
  @DisplayName("타임딜 생성 실패 - 저장 중 예외 발생")
  void createTimedeal_fail_saveException() {
    Product product = ProductFixture.createDefaultProduct();
    ReflectionTestUtils.setField(product, "id", 1L);
    Long productId = product.getId();

    TimedealCreateRequestDto dto =
        new TimedealCreateRequestDto(productId, FIXED_START, FIXED_END, 2000, 20, 150);
    when(productRepository.findById(productId)).thenReturn(Optional.of(product));
    when(timedealPolicyRepository.existsByProductIdAndTimeOverlap(eq(productId), any(), any()))
        .thenReturn(false);
    when(timedealPolicyRepository.save(any())).thenThrow(new RuntimeException(DB_ERROR_MESSAGE));

    CustomException exception =
        catchThrowableOfType(() -> service.create(dto), CustomException.class);

    assertThat(exception.getErrorCode()).isEqualTo(TimedealErrorCode.TIMEDEAL_SAVE_FAIL);
    assertThat(exception.getMessage()).isEqualTo(TimedealErrorCode.TIMEDEAL_SAVE_FAIL.getMessage());
  }
}
