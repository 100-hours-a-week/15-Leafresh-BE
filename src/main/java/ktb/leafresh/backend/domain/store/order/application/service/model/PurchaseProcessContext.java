package ktb.leafresh.backend.domain.store.order.application.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.store.order.domain.entity.enums.PurchaseType;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;

@Schema(description = "구매 처리 컨텍스트")
public record PurchaseProcessContext(
    @Schema(description = "구매자 회원 정보") Member member,
    @Schema(description = "구매할 상품 정보") Product product,
    @Schema(description = "구매 수량", example = "2") int quantity,
    @Schema(description = "단위 가격", example = "5000") int unitPrice,
    @Schema(
            description = "구매 타입",
            example = "NORMAL",
            allowableValues = {"NORMAL", "TIMEDEAL"})
        PurchaseType purchaseType) {

  @Schema(description = "총 구매 금액을 계산합니다")
  public int totalPrice() {
    return unitPrice * quantity;
  }
}
