package ktb.leafresh.backend.domain.store.order.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.store.order.domain.entity.ProductPurchase;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Schema(description = "상품 구매 요약 정보")
@Getter
@Builder
public class ProductPurchaseSummaryResponseDto {

  @Schema(description = "구매 ID", example = "1")
  private final Long id;

  @Schema(description = "구매한 상품 정보")
  private final ProductInfo product;

  @Schema(description = "구매 수량", example = "2")
  private final int quantity;

  @Schema(description = "구매 가격", example = "10000")
  private final int price;

  @Schema(description = "구매 일시", example = "2024-12-01T10:30:00Z")
  private final OffsetDateTime purchasedAt;

  @JsonIgnore private final String type;

  @Schema(description = "상품 기본 정보")
  @Getter
  @Builder
  public static class ProductInfo {
    @Schema(description = "상품 ID", example = "1")
    private final Long id;

    @Schema(description = "상품명", example = "신선한 사과")
    private final String title;

    @Schema(description = "상품 이미지 URL", example = "https://example.com/apple.jpg")
    private final String imageUrl;
  }

  public static ProductPurchaseSummaryResponseDto from(ProductPurchase entity) {
    return ProductPurchaseSummaryResponseDto.builder()
        .id(entity.getId())
        .product(
            ProductInfo.builder()
                .id(entity.getProduct().getId())
                .title(entity.getProduct().getName())
                .imageUrl(entity.getProduct().getImageUrl())
                .build())
        .quantity(entity.getQuantity())
        .price(entity.getPrice())
        .purchasedAt(entity.getPurchasedAt().atOffset(ZoneOffset.UTC))
        .type(entity.getType().name()) // JsonIgnore로 제외됨
        .build();
  }

  public static Long id(ProductPurchaseSummaryResponseDto dto) {
    return dto.getId();
  }

  public static LocalDateTime purchasedAt(ProductPurchaseSummaryResponseDto dto) {
    return dto.getPurchasedAt().toLocalDateTime();
  }
}
