package ktb.leafresh.backend.domain.store.order.application.service;

import ktb.leafresh.backend.domain.store.order.domain.entity.ProductPurchase;
import ktb.leafresh.backend.domain.store.order.infrastructure.repository.ProductPurchaseQueryRepository;
import ktb.leafresh.backend.domain.store.order.presentation.dto.response.ProductPurchaseListResponseDto;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import ktb.leafresh.backend.support.fixture.ProductFixture;
import ktb.leafresh.backend.support.fixture.ProductPurchaseFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductPurchaseReadServiceTest {

    @Mock
    private ProductPurchaseQueryRepository productPurchaseQueryRepository;

    @InjectMocks
    private ProductPurchaseReadService service;

    private final Member member = MemberFixture.of(1L, "buyer@leafresh.com", "구매자");
    private final Product product = ProductFixture.createActiveProduct("텀블러", 12000, 50);

    @Test
    @DisplayName("구매 목록을 커서 기반으로 조회한다")
    void getPurchases_success() {
        // given
        ProductPurchase purchase = ProductPurchaseFixture.of(member, product);
        given(productPurchaseQueryRepository.findByMemberWithCursorAndSearch(
                eq(member.getId()), eq("텀블러"), eq(null), eq(null), eq(10)
        )).willReturn(List.of(purchase));

        // when
        ProductPurchaseListResponseDto result = service.getPurchases(member.getId(), "텀블러", null, null, 10);

        // then
        assertThat(result.getPurchases()).hasSize(1);
        var purchaseDto = result.getPurchases().get(0);
        assertThat(purchaseDto.getProduct().getTitle()).isEqualTo(product.getName());
        assertThat(result.isHasNext()).isFalse();
    }
}
