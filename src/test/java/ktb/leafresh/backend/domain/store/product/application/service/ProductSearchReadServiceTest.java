package ktb.leafresh.backend.domain.store.product.application.service;

import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductSearchQueryRepository;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.ProductListResponseDto;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.ProductSummaryResponseDto;
import ktb.leafresh.backend.support.fixture.ProductFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSearchReadService 테스트")
class ProductSearchReadServiceTest {

    @Mock
    private ProductSearchQueryRepository productSearchQueryRepository;

    @InjectMocks
    private ProductSearchReadService productSearchReadService;

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2025, 1, 1, 12, 0);

    @Test
    @DisplayName("상품 검색 시 필터 조건에 맞는 상품 목록을 반환한다")
    void search_whenProductsFound_returnsProductList() {
        // given
        String keyword = "비누";
        Long cursorId = null;
        String cursorTimestamp = null;
        int size = 2;

        Product product1 = ProductFixture.createActiveProduct("유기농 비누", 3500, 10);
        Product product2 = ProductFixture.createActiveProduct("수제 비누", 4000, 8);

        ReflectionTestUtils.setField(product1, "createdAt", FIXED_TIME);
        ReflectionTestUtils.setField(product2, "createdAt", FIXED_TIME.minusMinutes(1));

        when(productSearchQueryRepository.findWithFilter(keyword, cursorId, cursorTimestamp, size))
                .thenReturn(List.of(product1, product2));

        // when
        ProductListResponseDto result = productSearchReadService.search(keyword, cursorId, cursorTimestamp, size);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getProducts()).hasSize(2);

        var resultProducts = result.getProducts();
        assertProductSummary(resultProducts.get(0), product1);
        assertProductSummary(resultProducts.get(1), product2);
    }

    @Test
    @DisplayName("상품 검색 결과가 없을 경우 빈 목록을 반환한다")
    void search_whenNoProducts_returnsEmptyList() {
        // given
        when(productSearchQueryRepository.findWithFilter(any(), any(), any(), anyInt()))
                .thenReturn(List.of());

        // when
        ProductListResponseDto result = productSearchReadService.search("없는상품", null, null, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getProducts()).isEmpty();
        assertThat(result.isHasNext()).isFalse();
    }

    private void assertProductSummary(ProductSummaryResponseDto actual, Product expected) {
        assertThat(actual.getTitle()).isEqualTo(expected.getName());
        assertThat(actual.getPrice()).isEqualTo(expected.getPrice());
        assertThat(actual.getStock()).isEqualTo(expected.getStock());
        assertThat(actual.getImageUrl()).isEqualTo(expected.getImageUrl());
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus().name());
    }
}
