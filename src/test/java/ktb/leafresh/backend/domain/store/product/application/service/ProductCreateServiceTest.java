package ktb.leafresh.backend.domain.store.product.application.service;

import ktb.leafresh.backend.domain.store.product.application.event.ProductUpdatedEvent;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.enums.ProductStatus;
import ktb.leafresh.backend.domain.store.product.domain.factory.ProductFactory;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.ProductCacheService;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
import ktb.leafresh.backend.domain.store.product.presentation.dto.request.ProductCreateRequestDto;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.ProductCreateResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ProductErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductCreateServiceTest {

    private ProductRepository productRepository;
    private ProductFactory productFactory;
    private ProductCacheService productCacheService;
    private ApplicationEventPublisher eventPublisher;
    private ProductCreateService service;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productFactory = mock(ProductFactory.class);
        productCacheService = mock(ProductCacheService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        service = new ProductCreateService(
                productRepository,
                productFactory,
                eventPublisher,
                productCacheService
        );
    }

    @Test
    @DisplayName("정상적으로 상품을 생성한다")
    void createProduct_success() {
        // given
        ProductCreateRequestDto dto = new ProductCreateRequestDto(
                "유기농 비누",
                "피부에 좋은 비누",
                "https://image.test/soap.png",
                3500,
                10,
                ProductStatus.ACTIVE
        );

        Product createdProduct = Product.builder()
                .id(1L)
                .name("유기농 비누")
                .description("피부에 좋은 비누")
                .imageUrl("https://image.test/soap.png")
                .price(3500)
                .stock(10)
                .build();

        when(productFactory.create(dto)).thenReturn(createdProduct);
        when(productRepository.save(createdProduct)).thenReturn(createdProduct);

        // when
        ProductCreateResponseDto response = service.createProduct(dto);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);

        verify(productFactory).create(dto);
        verify(productRepository).save(createdProduct);
        verify(productCacheService).cacheProductStock(1L, 10);

        ArgumentCaptor<ProductUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(ProductUpdatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().productId()).isEqualTo(1L);
        assertThat(eventCaptor.getValue().isTimeDeal()).isFalse();
    }

    @Test
    @DisplayName("상품 생성 중 예외 발생 시 PRODUCT_CREATE_FAILED 예외를 던진다")
    void createProduct_fail() {
        // given
        ProductCreateRequestDto dto = new ProductCreateRequestDto(
                "비건 로션",
                "피부 진정 효과",
                "https://image.test/lotion.png",
                5000,
                5,
                ProductStatus.ACTIVE // 또는 원하는 상태 값
        );

        when(productFactory.create(dto)).thenThrow(new RuntimeException("DB error"));

        // when
        CustomException exception = catchThrowableOfType(
                () -> service.createProduct(dto),
                CustomException.class
        );

        // then
        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_CREATE_FAILED);

        verify(productFactory).create(dto);
        verify(productRepository, never()).save(any());
        verify(productCacheService, never()).cacheProductStock(anyLong(), anyInt());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
