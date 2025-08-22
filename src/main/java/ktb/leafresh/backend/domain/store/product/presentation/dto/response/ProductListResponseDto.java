package ktb.leafresh.backend.domain.store.product.presentation.dto.response;

import ktb.leafresh.backend.global.util.pagination.CursorInfo;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProductListResponseDto {
  private final List<ProductSummaryResponseDto> products;
  private final boolean hasNext;
  private final CursorInfo cursorInfo;

  public static ProductListResponseDto from(
      CursorPaginationResult<ProductSummaryResponseDto> result) {
    return ProductListResponseDto.builder()
        .products(result.items())
        .hasNext(result.hasNext())
        .cursorInfo(result.cursorInfo())
        .build();
  }
}
