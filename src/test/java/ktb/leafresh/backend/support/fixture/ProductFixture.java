package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.enums.ProductStatus;

import java.util.ArrayList;

public class ProductFixture {

    public static Product createProduct(String name, int price, int stock, ProductStatus status) {
        return Product.builder()
                .name(name)
                .description("테스트 상품 설명")
                .imageUrl("https://dummy.image/product.png")
                .price(price)
                .stock(stock)
                .status(status)
                .timedealPolicies(new ArrayList<>())
                .build();
    }

    public static Product createActiveProduct(String name, int price, int stock) {
        return createProduct(name, price, stock, ProductStatus.ACTIVE);
    }

    public static Product createInactiveProduct(String name, int price, int stock) {
        return createProduct(name, price, stock, ProductStatus.INACTIVE);
    }

    public static Product createDefaultProduct() {
        return createActiveProduct("기본 상품", 3000, 10);
    }
}
