package ktb.leafresh.backend.domain.store.product.application.service;

import ktb.leafresh.backend.domain.store.order.application.facade.ProductCacheLockFacade;
import ktb.leafresh.backend.domain.store.product.application.event.ProductUpdatedEvent;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.enums.ProductStatus;
import ktb.leafresh.backend.domain.store.product.domain.factory.ProductFactory;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
import ktb.leafresh.backend.domain.store.product.presentation.dto.request.ProductCreateRequestDto;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.ProductCreateResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ProductErrorCode;
import ktb.leafresh.backend.support.fixture.ProductFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCreateServiceTest {

  @Mock private ProductRepository productRepository;

  @Mock private ProductFactory productFactory;

  @Mock private ProductCacheLockFacade productCacheLockFacade;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private ProductCreateService service;

  private static final String PRODUCT_NAME = "유기농 비누";
  private static final String PRODUCT_DESC = "피부에 좋은 비누";
  private static final String PRODUCT_IMAGE = "https://image.test/soap.png";
  private static final int PRICE = 3500;
  private static final int STOCK = 10;

  @Test
  @DisplayName("정상적으로 상품을 생성한다")
  void createProduct_success() {
    // given
    ProductCreateRequestDto dto =
        new ProductCreateRequestDto(
            PRODUCT_NAME, PRODUCT_DESC, PRODUCT_IMAGE, PRICE, STOCK, ProductStatus.ACTIVE);

    Product product = ProductFixture.create(PRODUCT_NAME, PRICE, STOCK, ProductStatus.ACTIVE);

    when(productFactory.create(dto)).thenReturn(product);
    when(productRepository.save(product)).thenReturn(product);

    // when
    ProductCreateResponseDto response = service.createProduct(dto);

    // then
    assertThat(response.id()).isEqualTo(product.getId());

    verify(productFactory).create(dto);
    verify(productRepository).save(product);
    verify(productCacheLockFacade).cacheProductStock(product.getId(), product.getStock());

    ArgumentCaptor<ProductUpdatedEvent> eventCaptor =
        ArgumentCaptor.forClass(ProductUpdatedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    ProductUpdatedEvent capturedEvent = eventCaptor.getValue();
    assertThat(capturedEvent.productId()).isEqualTo(product.getId());
    assertThat(capturedEvent.isTimeDeal()).isFalse();
  }

  @Test
  @DisplayName("상품 생성 중 예외 발생 시 PRODUCT_CREATE_FAILED 예외를 던진다")
  void createProduct_fail() {
    // given
    ProductCreateRequestDto dto =
        new ProductCreateRequestDto(
            "비건 로션", "피부 진정 효과", "https://image.test/lotion.png", 5000, 5, ProductStatus.ACTIVE);

    // when
    when(productFactory.create(dto)).thenThrow(new IllegalStateException("DB 에러 발생"));

    CustomException exception =
        catchThrowableOfType(() -> service.createProduct(dto), CustomException.class);

    // then
    assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_CREATE_FAILED);
    assertThat(exception.getMessage())
        .contains(ProductErrorCode.PRODUCT_CREATE_FAILED.getMessage());

    verify(productFactory).create(dto);
    verify(productRepository, never()).save(any());
    verify(productCacheLockFacade, never()).cacheProductStock(anyLong(), anyInt());
    verify(eventPublisher, never()).publishEvent(any());
  }
}
