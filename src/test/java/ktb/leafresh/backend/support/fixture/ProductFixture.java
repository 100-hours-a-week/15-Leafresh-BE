package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.enums.ProductStatus;

import java.util.ArrayList;

public class ProductFixture {

    public static Product of(String name, int price, int stock) {
        return Product.builder()
                .id(1L)
                .name(name)
                .description("테스트용 상품 설명")
                .imageUrl("https://dummy.image/product.png")
                .price(price)
                .stock(stock)
                .status(ProductStatus.ACTIVE)
                .timedealPolicies(new ArrayList<>())
                .build();
    }
}
