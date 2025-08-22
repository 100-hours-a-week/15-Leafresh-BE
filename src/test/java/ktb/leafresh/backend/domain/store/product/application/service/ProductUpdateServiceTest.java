package ktb.leafresh.backend.domain.store.product.application.service;

import ktb.leafresh.backend.domain.store.order.application.facade.ProductCacheLockFacade;
import ktb.leafresh.backend.domain.store.product.application.event.ProductUpdatedEvent;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.enums.ProductStatus;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
import ktb.leafresh.backend.domain.store.product.presentation.dto.request.ProductUpdateRequestDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ProductErrorCode;
import ktb.leafresh.backend.support.fixture.ProductFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class ProductUpdateServiceTest {

  @Mock private ProductRepository productRepository;

  @Mock private ProductCacheLockFacade productCacheLockFacade;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private ProductUpdateService productUpdateService;

  private Product product;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    product = ProductFixture.createDefaultProduct();
  }

  @Test
  void update_withValidData_success() {
    // given
    given(productRepository.findById(anyLong())).willReturn(Optional.of(product));
    ProductUpdateRequestDto dto =
        new ProductUpdateRequestDto(
            "새 이름", "새 설명", "https://image.new.png", 5000, 20, ProductStatus.INACTIVE.name());

    // when
    productUpdateService.update(1L, dto);

    // then
    assertThat(product.getName()).isEqualTo(dto.name());
    assertThat(product.getDescription()).isEqualTo(dto.description());
    assertThat(product.getImageUrl()).isEqualTo(dto.imageUrl());
    assertThat(product.getPrice()).isEqualTo(dto.price());
    assertThat(product.getStock()).isEqualTo(dto.stock());
    assertThat(product.getStatus().name()).isEqualTo(dto.status());

    then(productCacheLockFacade).should().cacheProductStock(product.getId(), dto.stock());
    then(productCacheLockFacade).should().evictCacheByProduct(product);
    then(eventPublisher).should().publishEvent(any(ProductUpdatedEvent.class));
  }

  @Test
  void update_withNegativePrice_throwsException() {
    // given
    given(productRepository.findById(anyLong())).willReturn(Optional.of(product));
    ProductUpdateRequestDto dto = new ProductUpdateRequestDto(null, null, null, -1000, null, null);

    // when & then
    assertThatThrownBy(() -> productUpdateService.update(1L, dto))
        .isInstanceOf(CustomException.class)
        .hasMessage(ProductErrorCode.INVALID_PRICE.getMessage());
  }

  @Test
  void update_withNegativeStock_throwsException() {
    // given
    given(productRepository.findById(anyLong())).willReturn(Optional.of(product));
    ProductUpdateRequestDto dto = new ProductUpdateRequestDto(null, null, null, null, -5, null);

    // when & then
    assertThatThrownBy(() -> productUpdateService.update(1L, dto))
        .isInstanceOf(CustomException.class)
        .hasMessage(ProductErrorCode.INVALID_STOCK.getMessage());
  }

  @Test
  void update_withInvalidStatus_throwsException() {
    // given
    given(productRepository.findById(anyLong())).willReturn(Optional.of(product));
    ProductUpdateRequestDto dto =
        new ProductUpdateRequestDto(null, null, null, null, null, "INVALID_STATUS");

    // when & then
    assertThatThrownBy(() -> productUpdateService.update(1L, dto))
        .isInstanceOf(CustomException.class)
        .hasMessage(ProductErrorCode.INVALID_STATUS.getMessage());
  }

  @Test
  void update_whenProductNotFound_throwsException() {
    // given
    given(productRepository.findById(anyLong())).willReturn(Optional.empty());
    ProductUpdateRequestDto dto = new ProductUpdateRequestDto("새 이름", null, null, null, null, null);

    // when & then
    assertThatThrownBy(() -> productUpdateService.update(1L, dto))
        .isInstanceOf(CustomException.class)
        .hasMessage(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage());
  }
}
