package ktb.leafresh.backend.domain.store.product.application.service;

import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductSearchQueryRepository;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.ProductListResponseDto;
import ktb.leafresh.backend.support.fixture.ProductFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductSearchReadServiceTest {

    private ProductSearchQueryRepository productSearchQueryRepository;
    private ProductSearchReadService productSearchReadService;

    @BeforeEach
    void setUp() {
        productSearchQueryRepository = mock(ProductSearchQueryRepository.class);
        productSearchReadService = new ProductSearchReadService(productSearchQueryRepository);
    }

    @Test
    @DisplayName("상품 검색 결과를 정상적으로 반환한다")
    void search_success() {
        String keyword = "비누";
        Long cursorId = null;
        String cursorTimestamp = null;
        int size = 2;

        Product p1 = ProductFixture.of("유기농 비누", 3500, 10);
        Product p2 = ProductFixture.of("수제 비누", 4000, 8);

        ReflectionTestUtils.setField(p1, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(p2, "createdAt", LocalDateTime.now().minusMinutes(1));

        when(productSearchQueryRepository.findWithFilter(keyword, cursorId, cursorTimestamp, size))
                .thenReturn(List.of(p1, p2));

        ProductListResponseDto result = productSearchReadService.search(keyword, cursorId, cursorTimestamp, size);

        assertThat(result).isNotNull();
        assertThat(result.getProducts()).hasSize(2);
        assertThat(result.getProducts().get(0).getTitle()).contains("비누");
    }

    @Test
    @DisplayName("검색 결과가 비어 있으면 빈 목록을 반환한다")
    void search_emptyResult() {
        // given
        when(productSearchQueryRepository.findWithFilter(any(), any(), any(), anyInt()))
                .thenReturn(List.of());

        // when
        ProductListResponseDto result = productSearchReadService.search("없는상품", null, null, 10);

        // then
        assertThat(result.getProducts()).isEmpty();
        assertThat(result.isHasNext()).isFalse();
    }
}
