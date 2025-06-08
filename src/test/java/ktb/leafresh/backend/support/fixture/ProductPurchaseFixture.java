package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.store.order.domain.entity.ProductPurchase;
import ktb.leafresh.backend.domain.store.order.domain.entity.enums.PurchaseType;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;

import java.time.LocalDateTime;

public class ProductPurchaseFixture {

    public static ProductPurchase of(Member member, Product product) {
        return ProductPurchase.builder()
                .id(1L)
                .member(member)
                .product(product)
                .type(PurchaseType.NORMAL)
                .price(product.getPrice())
                .quantity(1)
                .purchasedAt(LocalDateTime.now())
                .build();
    }
}
