package ktb.leafresh.backend.domain.store.order.application.service;

import ktb.leafresh.backend.domain.store.order.domain.entity.ProductPurchase;
import ktb.leafresh.backend.domain.store.order.infrastructure.repository.ProductPurchaseQueryRepository;
import ktb.leafresh.backend.domain.store.order.presentation.dto.response.ProductPurchaseListResponseDto;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import ktb.leafresh.backend.support.fixture.ProductFixture;
import ktb.leafresh.backend.support.fixture.ProductPurchaseFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ProductPurchaseReadServiceTest {

    private ProductPurchaseQueryRepository productPurchaseQueryRepository;
    private ProductPurchaseReadService service;

    private Member member;
    private Product product;

    @BeforeEach
    void setUp() {
        productPurchaseQueryRepository = mock(ProductPurchaseQueryRepository.class);
        service = new ProductPurchaseReadService(productPurchaseQueryRepository);

        member = MemberFixture.of(1L, "buyer@leafresh.com", "구매자");
        product = ProductFixture.of("텀블러", 12000, 50);
    }

    @Test
    @DisplayName("구매 목록을 커서 기반으로 조회한다")
    void getPurchases_success() {
        // given
        ProductPurchase purchase = ProductPurchaseFixture.of(member, product);
        when(productPurchaseQueryRepository.findByMemberWithCursorAndSearch(
                eq(1L), eq("텀블러"), eq(null), eq(null), eq(10)
        )).thenReturn(List.of(purchase));

        // when
        ProductPurchaseListResponseDto result = service.getPurchases(1L, "텀블러", null, null, 10);

        // then
        assertThat(result.getPurchases()).hasSize(1);
        assertThat(result.getPurchases().get(0).getProduct().getTitle()).isEqualTo("텀블러");
        assertThat(result.isHasNext()).isFalse(); // 한 개만 조회됐으므로 next 없음
    }
}
