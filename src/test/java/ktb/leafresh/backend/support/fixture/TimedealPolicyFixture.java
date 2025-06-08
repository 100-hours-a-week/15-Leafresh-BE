package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;

import java.time.LocalDateTime;

public class TimedealPolicyFixture {

    public static TimedealPolicy of(Product product) {
        return TimedealPolicy.builder()
                .id(1L)
                .product(product)
                .discountedPrice(2500)
                .discountedPercentage(30)
                .stock(10)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusHours(1))
                .build();
    }

    public static TimedealPolicy expired(Product product) {
        return TimedealPolicy.builder()
                .id(2L)
                .product(product)
                .discountedPrice(2900)
                .discountedPercentage(20)
                .stock(5)
                .startTime(LocalDateTime.now().minusDays(2))
                .endTime(LocalDateTime.now().minusDays(1))
                .build();
    }

    public static TimedealPolicy upcoming(Product product) {
        return TimedealPolicy.builder()
                .id(3L)
                .product(product)
                .discountedPrice(2700)
                .discountedPercentage(25)
                .stock(20)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .build();
    }
}
