package ktb.leafresh.backend.domain.store.product.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Schema(description = "상품 요약 정보")
@Getter
@Builder
public class ProductSummaryResponseDto {

  @Schema(description = "상품 ID", example = "1")
  private final Long id;

  @Schema(description = "상품명", example = "신선한 사과")
  private final String title;

  @Schema(description = "상품 설명", example = "당도 높은 국산 사과입니다.")
  private final String description;

  @Schema(description = "상품 이미지 URL", example = "https://example.com/apple.jpg")
  private final String imageUrl;

  @Schema(description = "상품 가격", example = "5000")
  private final int price;

  @Schema(description = "재고 수량", example = "100")
  private final int stock;

  @Schema(
      description = "상품 상태",
      example = "ACTIVE",
      allowableValues = {"ACTIVE", "SOLD_OUT", "HIDDEN", "INACTIVE", "DELETED"})
  private final String status;

  @JsonIgnore private final OffsetDateTime createdAt;

  public static ProductSummaryResponseDto from(Product product) {
    return ProductSummaryResponseDto.builder()
        .id(product.getId())
        .title(product.getName())
        .description(product.getDescription())
        .imageUrl(product.getImageUrl())
        .price(product.getPrice())
        .stock(product.getStock())
        .status(product.getStatus().name())
        .createdAt(product.getCreatedAt().atOffset(ZoneOffset.UTC))
        .build();
  }
}
