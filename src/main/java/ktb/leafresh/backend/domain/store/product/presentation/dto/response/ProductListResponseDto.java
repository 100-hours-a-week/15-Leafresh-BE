package ktb.leafresh.backend.domain.store.product.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.global.util.pagination.CursorInfo;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Schema(description = "상품 목록 응답")
@Getter
@Builder
public class ProductListResponseDto {
  @Schema(description = "상품 목록")
  private final List<ProductSummaryResponseDto> products;

  @Schema(description = "다음 페이지 존재 여부", example = "true")
  private final boolean hasNext;

  @Schema(description = "커서 페이지네이션 정보")
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
