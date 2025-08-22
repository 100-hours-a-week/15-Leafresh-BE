package ktb.leafresh.backend.domain.store.order.infrastructure.publisher;

import ktb.leafresh.backend.domain.store.order.application.dto.PurchaseCommand;

public interface PurchaseMessagePublisher {
  void publish(PurchaseCommand command);
}
