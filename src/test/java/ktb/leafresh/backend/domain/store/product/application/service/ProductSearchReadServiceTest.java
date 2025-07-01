package ktb.leafresh.backend.domain.store.product.application.service;

import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductSearchQueryRepository;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.ProductListResponseDto;
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
    @DisplayName("상품 검색 결과를 정상적으로 반환한다")
    void search_success() {
        // given
        String keyword = "비누";
        Long cursorId = null;
        String cursorTimestamp = null;
        int size = 2;

        Product p1 = ProductFixture.createActiveProduct("유기농 비누", 3500, 10);
        Product p2 = ProductFixture.createActiveProduct("수제 비누", 4000, 8);

        ReflectionTestUtils.setField(p1, "createdAt", FIXED_TIME);
        ReflectionTestUtils.setField(p2, "createdAt", FIXED_TIME.minusMinutes(1));

        when(productSearchQueryRepository.findWithFilter(keyword, cursorId, cursorTimestamp, size))
                .thenReturn(List.of(p1, p2));

        // when
        ProductListResponseDto result = productSearchReadService.search(keyword, cursorId, cursorTimestamp, size);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getProducts()).hasSize(2);

        var response1 = result.getProducts().get(0);
        assertThat(response1.getTitle()).isEqualTo(p1.getName());
        assertThat(response1.getPrice()).isEqualTo(p1.getPrice());
        assertThat(response1.getStock()).isEqualTo(p1.getStock());
        assertThat(response1.getImageUrl()).isEqualTo(p1.getImageUrl());
        assertThat(response1.getStatus()).isEqualTo(p1.getStatus().name());

        var response2 = result.getProducts().get(1);
        assertThat(response2.getTitle()).isEqualTo(p2.getName());
        assertThat(response2.getPrice()).isEqualTo(p2.getPrice());
        assertThat(response2.getStock()).isEqualTo(p2.getStock());
        assertThat(response2.getImageUrl()).isEqualTo(p2.getImageUrl());
        assertThat(response2.getStatus()).isEqualTo(p2.getStatus().name());
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
        assertThat(result).isNotNull();
        assertThat(result.getProducts()).isEmpty();
        assertThat(result.isHasNext()).isFalse();
    }
}
