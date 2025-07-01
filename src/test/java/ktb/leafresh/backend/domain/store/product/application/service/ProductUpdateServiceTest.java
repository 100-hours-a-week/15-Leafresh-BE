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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductUpdateService 테스트")
class ProductUpdateServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCacheLockFacade productCacheLockFacade;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProductUpdateService productUpdateService;

    @Test
    @DisplayName("상품을 정상적으로 수정한다")
    void update_success() {
        // given
        Product product = ProductFixture.createDefaultProduct();
        Long productId = 1L;
        ReflectionTestUtils.setField(product, "id", productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ProductUpdateRequestDto dto = new ProductUpdateRequestDto(
                "수정상품", "설명", "https://new.image/product.png", 2000, 15, ProductStatus.INACTIVE.name()
        );

        // when
        productUpdateService.update(productId, dto);

        // then
        assertThat(product.getName()).isEqualTo(dto.name());
        assertThat(product.getDescription()).isEqualTo(dto.description());
        assertThat(product.getImageUrl()).isEqualTo(dto.imageUrl());
        assertThat(product.getPrice()).isEqualTo(dto.price());
        assertThat(product.getStock()).isEqualTo(dto.stock());
        assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);

        verify(productCacheLockFacade).cacheProductStock(productId, dto.stock());
        verify(productCacheLockFacade).evictCacheByProduct(product);

        ArgumentCaptor<ProductUpdatedEvent> captor = ArgumentCaptor.forClass(ProductUpdatedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().productId()).isEqualTo(productId);
    }

    @Test
    @DisplayName("상품이 존재하지 않으면 예외 발생")
    void update_fail_product_not_found() {
        // given
        Long invalidId = 999L;
        ProductUpdateRequestDto dto = new ProductUpdateRequestDto("name", "desc", "url", 1000, 5, ProductStatus.ACTIVE.name());
        when(productRepository.findById(invalidId)).thenReturn(Optional.empty());

        // when
        CustomException ex = catchThrowableOfType(() ->
                productUpdateService.update(invalidId, dto), CustomException.class);

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("유효하지 않은 가격이면 예외 발생")
    void update_fail_invalid_price() {
        // given
        Product product = ProductFixture.createDefaultProduct();
        Long id = 1L;
        ReflectionTestUtils.setField(product, "id", id);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        ProductUpdateRequestDto dto = new ProductUpdateRequestDto(null, null, null, 0, null, null);

        // when
        CustomException ex = catchThrowableOfType(() ->
                productUpdateService.update(id, dto), CustomException.class);

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ProductErrorCode.INVALID_PRICE);
    }

    @Test
    @DisplayName("재고가 음수이면 예외 발생")
    void update_fail_invalid_stock() {
        // given
        Product product = ProductFixture.createDefaultProduct();
        Long id = 1L;
        ReflectionTestUtils.setField(product, "id", id);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        ProductUpdateRequestDto dto = new ProductUpdateRequestDto(null, null, null, null, -1, null);

        // when
        CustomException ex = catchThrowableOfType(() ->
                productUpdateService.update(id, dto), CustomException.class);

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ProductErrorCode.INVALID_STOCK);
    }

    @Test
    @DisplayName("유효하지 않은 상태값이면 예외 발생")
    void update_fail_invalid_status() {
        // given
        Product product = ProductFixture.createDefaultProduct();
        Long id = 1L;
        ReflectionTestUtils.setField(product, "id", id);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        ProductUpdateRequestDto dto = new ProductUpdateRequestDto(null, null, null, null, null, "WRONG");

        // when
        CustomException ex = catchThrowableOfType(() ->
                productUpdateService.update(id, dto), CustomException.class);

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ProductErrorCode.INVALID_STATUS);
    }
}
