package ktb.leafresh.backend.domain.store.product.application.service;

import ktb.leafresh.backend.domain.store.product.application.event.ProductUpdatedEvent;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.enums.ProductStatus;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.ProductCacheService;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
import ktb.leafresh.backend.domain.store.product.presentation.dto.request.ProductUpdateRequestDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ProductErrorCode;
import ktb.leafresh.backend.support.fixture.ProductFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductUpdateServiceTest {

    private ProductRepository productRepository;
    private ProductCacheService productCacheService;
    private ApplicationEventPublisher eventPublisher;
    private ProductUpdateService productUpdateService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productCacheService = mock(ProductCacheService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        productUpdateService = new ProductUpdateService(productRepository, productCacheService, eventPublisher);
    }

    @Test
    @DisplayName("상품을 정상적으로 수정한다")
    void update_success() {
        // given
        Product product = ProductFixture.of("기존상품", 1000, 5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductUpdateRequestDto dto = new ProductUpdateRequestDto(
                "수정상품", "설명", "https://new.image/product.png", 2000, 15, "INACTIVE"
        );

        // when
        productUpdateService.update(1L, dto);

        // then
        assertThat(product.getName()).isEqualTo("수정상품");
        assertThat(product.getDescription()).isEqualTo("설명");
        assertThat(product.getImageUrl()).isEqualTo("https://new.image/product.png");
        assertThat(product.getPrice()).isEqualTo(2000);
        assertThat(product.getStock()).isEqualTo(15);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);

        verify(productCacheService).cacheProductStock(1L, 15);
        verify(productCacheService).evictCacheByProduct(product);

        ArgumentCaptor<ProductUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(ProductUpdatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().productId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("상품이 존재하지 않으면 예외 발생")
    void update_fail_product_not_found() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ProductUpdateRequestDto dto = new ProductUpdateRequestDto("name", "desc", "url", 1000, 5, "ACTIVE");

        CustomException ex = catchThrowableOfType(() -> productUpdateService.update(1L, dto), CustomException.class);

        assertThat(ex.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("유효하지 않은 가격이면 예외 발생")
    void update_fail_invalid_price() {
        Product product = ProductFixture.of("상품", 1000, 5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductUpdateRequestDto dto = new ProductUpdateRequestDto(null, null, null, 0, null, null);

        CustomException ex = catchThrowableOfType(() -> productUpdateService.update(1L, dto), CustomException.class);

        assertThat(ex.getErrorCode()).isEqualTo(ProductErrorCode.INVALID_PRICE);
    }

    @Test
    @DisplayName("재고가 음수이면 예외 발생")
    void update_fail_invalid_stock() {
        Product product = ProductFixture.of("상품", 1000, 5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductUpdateRequestDto dto = new ProductUpdateRequestDto(null, null, null, null, -1, null);

        CustomException ex = catchThrowableOfType(() -> productUpdateService.update(1L, dto), CustomException.class);

        assertThat(ex.getErrorCode()).isEqualTo(ProductErrorCode.INVALID_STOCK);
    }

    @Test
    @DisplayName("유효하지 않은 상태값이면 예외 발생")
    void update_fail_invalid_status() {
        Product product = ProductFixture.of("상품", 1000, 5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductUpdateRequestDto dto = new ProductUpdateRequestDto(null, null, null, null, null, "WRONG_STATUS");

        CustomException ex = catchThrowableOfType(() -> productUpdateService.update(1L, dto), CustomException.class);

        assertThat(ex.getErrorCode()).isEqualTo(ProductErrorCode.INVALID_STATUS);
    }
}
