package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;

import java.time.LocalDateTime;

public class TimedealPolicyFixture {

  private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2025, 7, 1, 12, 0);

  public static TimedealPolicy createTimedeal(
      Product product,
      int price,
      int percentage,
      int stock,
      LocalDateTime start,
      LocalDateTime end) {
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
    LocalDateTime start = FIXED_NOW.minusHours(1);
    LocalDateTime end = FIXED_NOW.plusHours(1);
    return createTimedeal(product, 2500, 30, 10, start, end);
  }

  public static TimedealPolicy createExpiredTimedeal(Product product) {
    LocalDateTime start = FIXED_NOW.minusDays(2);
    LocalDateTime end = FIXED_NOW.minusDays(1);
    return createTimedeal(product, 2900, 20, 5, start, end);
  }

  public static TimedealPolicy createUpcomingTimedeal(Product product) {
    LocalDateTime start = FIXED_NOW.plusDays(1);
    LocalDateTime end = FIXED_NOW.plusDays(2);
    return createTimedeal(product, 2700, 25, 20, start, end);
  }

  public static TimedealPolicy createDefaultTimedeal(Product product) {
    return createOngoingTimedeal(product);
  }

  public static TimedealPolicy createCustomTimedeal(
      Product product,
      int price,
      int percentage,
      int stock,
      int startOffsetMinutes,
      int endOffsetMinutes) {
    LocalDateTime start = FIXED_NOW.plusMinutes(startOffsetMinutes);
    LocalDateTime end = FIXED_NOW.plusMinutes(endOffsetMinutes);
    return createTimedeal(product, price, percentage, stock, start, end);
  }
}
