package ktb.leafresh.backend.domain.store.order.presentation.dto.response;

import ktb.leafresh.backend.global.util.pagination.CursorInfo;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProductPurchaseListResponseDto {

  private final List<ProductPurchaseSummaryResponseDto> purchases;
  private final boolean hasNext;
  private final CursorInfo cursorInfo;

  public static ProductPurchaseListResponseDto from(
      CursorPaginationResult<ProductPurchaseSummaryResponseDto> result) {
    return ProductPurchaseListResponseDto.builder()
        .purchases(result.items())
        .hasNext(result.hasNext())
        .cursorInfo(result.cursorInfo())
        .build();
  }
}
