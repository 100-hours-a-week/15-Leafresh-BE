package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.store.order.domain.entity.ProductPurchase;
import ktb.leafresh.backend.domain.store.order.domain.entity.enums.PurchaseType;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;

import java.time.LocalDateTime;

public class ProductPurchaseFixture {

    private static final LocalDateTime FIXED_PURCHASED_AT = LocalDateTime.of(2024, 1, 1, 0, 0);

    public static ProductPurchase create(Member member, Product product) {
        return ProductPurchase.builder()
                .member(member)
                .product(product)
                .type(PurchaseType.NORMAL)
                .price(product.getPrice())
                .quantity(1)
                .purchasedAt(FIXED_PURCHASED_AT)
                .build();
    }

    public static ProductPurchase createWithType(Member member, Product product, PurchaseType type) {
        return ProductPurchase.builder()
                .member(member)
                .product(product)
                .type(type)
                .price(product.getPrice())
                .quantity(1)
                .purchasedAt(FIXED_PURCHASED_AT)
                .build();
    }
}
