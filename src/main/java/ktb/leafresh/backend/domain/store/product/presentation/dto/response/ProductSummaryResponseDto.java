package ktb.leafresh.backend.domain.store.product.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Getter
@Builder
public class ProductSummaryResponseDto {

  private final Long id;
  private final String title;
  private final String description;
  private final String imageUrl;
  private final int price;
  private final int stock;
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
