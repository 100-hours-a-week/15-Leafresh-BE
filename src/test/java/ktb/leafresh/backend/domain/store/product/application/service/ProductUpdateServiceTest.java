//package ktb.leafresh.backend.domain.store.product.application.service;
//
//import ktb.leafresh.backend.domain.store.order.application.facade.ProductCacheLockFacade;
//import ktb.leafresh.backend.domain.store.product.application.event.ProductUpdatedEvent;
//import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
//import ktb.leafresh.backend.domain.store.product.domain.entity.enums.ProductStatus;
//import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
//import ktb.leafresh.backend.domain.store.product.presentation.dto.request.ProductUpdateRequestDto;
//import ktb.leafresh.backend.global.exception.CustomException;
//import ktb.leafresh.backend.global.exception.ProductErrorCode;
//import ktb.leafresh.backend.support.fixture.ProductFixture;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("ProductUpdateService 테스트")
//class ProductUpdateServiceTest {
//
//    private static final Long FIXED_PRODUCT_ID = 1L;
//
//    @Mock
//    private ProductRepository productRepository;
//
//    @Mock
//    private ProductCacheLockFacade productCacheLockFacade;
//
//    @Mock
//    private ApplicationEventPublisher eventPublisher;
//
//    @InjectMocks
//    private ProductUpdateService productUpdateService;
//
//    @Test
//    @DisplayName("상품을 정상적으로 수정한다")
//    void update_success() {
//        // given
//        Product product = ProductFixture.createDefaultProduct();
//        ReflectionTestUtils.setField(product, "id", FIXED_PRODUCT_ID);
//
//        ProductUpdateRequestDto dto = new ProductUpdateRequestDto(
//                "수정상품",
//                "수정 설명",
//                "https://new.image/product.png",
//                2000,
//                15,
//                ProductStatus.INACTIVE.name()
//        );
//
//        when(productRepository.findById(FIXED_PRODUCT_ID)).thenReturn(Optional.of(product));
//
//        // when
//        productUpdateService.update(FIXED_PRODUCT_ID, dto);
//
//        // then
//        assertThat(product.getName()).isEqualTo(dto.name());
//        assertThat(product.getDescription()).isEqualTo(dto.description());
//        assertThat(product.getImageUrl()).isEqualTo(dto.imageUrl());
//        assertThat(product.getPrice()).isEqualTo(dto.price());
//        assertThat(product.getStock()).isEqualTo(dto.stock());
//        assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
//
//        verify(productCacheLockFacade).cacheProductStock(FIXED_PRODUCT_ID, dto.stock());
//        verify(productCacheLockFacade).evictCacheByProduct(product);
//
//        ArgumentCaptor<ProductUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(ProductUpdatedEvent.class);
//        verify(eventPublisher).publishEvent(eventCaptor.capture());
//        assertThat(eventCaptor.getValue().productId()).isEqualTo(FIXED_PRODUCT_ID);
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 상품이면 예외 발생")
//    void update_fail_product_not_found() {
//        // given
//        Long invalidId = 999L;
//        ProductUpdateRequestDto dto = createValidDto();
//        when(productRepository.findById(invalidId)).thenReturn(Optional.empty());
//
//        // when
//        CustomException exception = catchThrowableOfType(
//                () -> productUpdateService.update(invalidId, dto),
//                CustomException.class
//        );
//
//        // then
//        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.PRODUCT_NOT_FOUND);
//    }
//
//    @Test
//    @DisplayName("가격이 0원 이하면 예외 발생")
//    void update_fail_invalid_price() {
//        // given
//        Product product = ProductFixture.createDefaultProduct();
//        ReflectionTestUtils.setField(product, "id", FIXED_PRODUCT_ID);
//        when(productRepository.findById(FIXED_PRODUCT_ID)).thenReturn(Optional.of(product));
//
//        ProductUpdateRequestDto dto = new ProductUpdateRequestDto(null, null, null, 0, null, null);
//
//        // when
//        CustomException exception = catchThrowableOfType(
//                () -> productUpdateService.update(FIXED_PRODUCT_ID, dto),
//                CustomException.class
//        );
//
//        // then
//        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.INVALID_PRICE);
//    }
//
//    @Test
//    @DisplayName("재고가 음수면 예외 발생")
//    void update_fail_invalid_stock() {
//        // given
//        Product product = ProductFixture.createDefaultProduct();
//        ReflectionTestUtils.setField(product, "id", FIXED_PRODUCT_ID);
//        when(productRepository.findById(FIXED_PRODUCT_ID)).thenReturn(Optional.of(product));
//
//        ProductUpdateRequestDto dto = new ProductUpdateRequestDto(null, null, null, null, -10, null);
//
//        // when
//        CustomException exception = catchThrowableOfType(
//                () -> productUpdateService.update(FIXED_PRODUCT_ID, dto),
//                CustomException.class
//        );
//
//        // then
//        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.INVALID_STOCK);
//    }
//
//    @Test
//    @DisplayName("상태값이 잘못된 경우 예외 발생")
//    void update_fail_invalid_status() {
//        // given
//        Product product = ProductFixture.createDefaultProduct();
//        ReflectionTestUtils.setField(product, "id", FIXED_PRODUCT_ID);
//        when(productRepository.findById(FIXED_PRODUCT_ID)).thenReturn(Optional.of(product));
//
//        ProductUpdateRequestDto dto = new ProductUpdateRequestDto(null, null, null, null, null, "WRONG_STATUS");
//
//        // when
//        CustomException exception = catchThrowableOfType(
//                () -> productUpdateService.update(FIXED_PRODUCT_ID, dto),
//                CustomException.class
//        );
//
//        // then
//        assertThat(exception.getErrorCode()).isEqualTo(ProductErrorCode.INVALID_STATUS);
//    }
//
//    // 헬퍼 메서드
//    private ProductUpdateRequestDto createValidDto() {
//        return new ProductUpdateRequestDto(
//                "테스트 상품",
//                "테스트 설명",
//                "https://image.test/product.png",
//                1000,
//                5,
//                ProductStatus.ACTIVE.name()
//        );
//    }
//}
