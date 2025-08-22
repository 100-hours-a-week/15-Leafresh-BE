package ktb.leafresh.backend.domain.store.product.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Schema(description = "타임딜 상품 요약 정보")
public record TimedealProductSummaryResponseDto(
    @Schema(description = "타임딜 ID", example = "1") Long dealId,
    @Schema(description = "상품 ID", example = "1") Long productId,
    @Schema(description = "상품명", example = "신선한 사과") String title,
    @Schema(description = "상품 설명", example = "당도 높은 국산 사과입니다.") String description,
    @Schema(description = "원래 가격", example = "5000") int defaultPrice,
    @Schema(description = "할인된 가격", example = "3000") int discountedPrice,
    @Schema(description = "할인율 (%)", example = "40") int discountedPercentage,
    @Schema(description = "타임딜 재고 수량", example = "50") int stock,
    @Schema(description = "상품 이미지 URL", example = "https://example.com/apple.jpg") String imageUrl,
    @Schema(description = "타임딜 시작 시간", example = "2024-12-01T10:00:00Z")
        OffsetDateTime dealStartTime,
    @Schema(description = "타임딜 종료 시간", example = "2024-12-01T23:59:59Z") OffsetDateTime dealEndTime,
    @Schema(
            description = "상품 상태",
            example = "ACTIVE",
            allowableValues = {"ACTIVE", "SOLD_OUT", "HIDDEN", "INACTIVE", "DELETED"})
        String productStatus,
    @Schema(
            description = "타임딜 상태",
            example = "ONGOING",
            allowableValues = {"ONGOING", "UPCOMING"})
        String timeDealStatus) {
  public TimedealProductSummaryResponseDto(
      Long dealId,
      Long productId,
      String title,
      String description,
      int defaultPrice,
      int discountedPrice,
      int discountedPercentage,
      int stock,
      String imageUrl,
      LocalDateTime dealStartTime,
      LocalDateTime dealEndTime,
      String productStatus,
      String timeDealStatus) {
    this(
        dealId,
        productId,
        title,
        description,
        defaultPrice,
        discountedPrice,
        discountedPercentage,
        stock,
        imageUrl,
        dealStartTime.atOffset(ZoneOffset.UTC),
        dealEndTime.atOffset(ZoneOffset.UTC),
        productStatus,
        timeDealStatus);
  }

  public static TimedealProductSummaryResponseDto from(TimedealPolicy policy) {
    var product = policy.getProduct();
    var now = LocalDateTime.now(ZoneOffset.UTC);

    String productStatus =
        switch (product.getStatus()) {
          case SOLD_OUT -> "SOLD_OUT";
          case ACTIVE -> "ACTIVE";
          default -> "INACTIVE";
        };

    String timeDealStatus = now.isBefore(policy.getStartTime()) ? "UPCOMING" : "ONGOING";

    return new TimedealProductSummaryResponseDto(
        policy.getId(),
        product.getId(),
        product.getName(),
        product.getDescription(),
        product.getPrice(),
        policy.getDiscountedPrice(),
        policy.getDiscountedPercentage(),
        policy.getStock(),
        product.getImageUrl(),
        policy.getStartTime(),
        policy.getEndTime(),
        productStatus,
        timeDealStatus);
  }
}
