package ktb.leafresh.backend.domain.store.order.application.service.model;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.store.order.domain.entity.enums.PurchaseType;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;

public record PurchaseProcessContext(
    Member member, Product product, int quantity, int unitPrice, PurchaseType purchaseType) {
  public int totalPrice() {
    return unitPrice * quantity;
  }
}
