package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.store.order.domain.entity.PurchaseProcessingLog;
import ktb.leafresh.backend.domain.store.order.domain.entity.enums.PurchaseProcessingStatus;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;

public class PurchaseProcessingLogFixture {

  public static PurchaseProcessingLog of(
      Product product, PurchaseProcessingStatus status, String message) {
    return PurchaseProcessingLog.builder().product(product).status(status).message(message).build();
  }
}
