package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;

import java.time.LocalDateTime;

public class TimedealPolicyFixture {

    public static TimedealPolicy createTimedeal(Product product, int price, int percentage, int stock,
                                                LocalDateTime start, LocalDateTime end) {
        return TimedealPolicy.builder()
                .product(product)
                .discountedPrice(price)
                .discountedPercentage(percentage)
                .stock(stock)
                .startTime(start)
                .endTime(end)
                .build();
    }

    public static TimedealPolicy createOngoingTimedeal(Product product) {
        return createTimedeal(
                product,
                2500,
                30,
                10,
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1)
        );
    }

    public static TimedealPolicy createExpiredTimedeal(Product product) {
        return createTimedeal(
                product,
                2900,
                20,
                5,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1)
        );
    }

    public static TimedealPolicy createUpcomingTimedeal(Product product) {
        return createTimedeal(
                product,
                2700,
                25,
                20,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );
    }

    public static TimedealPolicy createDefaultTimedeal(Product product) {
        return createOngoingTimedeal(product);
    }

    public static TimedealPolicy createCustomTimedeal(Product product, int price, int percentage, int stock,
                                                      int startOffsetMinutes, int endOffsetMinutes) {
        LocalDateTime now = LocalDateTime.now();
        return createTimedeal(
                product,
                price,
                percentage,
                stock,
                now.plusMinutes(startOffsetMinutes),
                now.plusMinutes(endOffsetMinutes)
        );
    }
}
