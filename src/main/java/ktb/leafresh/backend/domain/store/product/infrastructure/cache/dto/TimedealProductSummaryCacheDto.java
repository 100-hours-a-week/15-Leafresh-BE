package ktb.leafresh.backend.domain.store.product.infrastructure.cache.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "타임딜 상품 요약 캐시 데이터")
public record TimedealProductSummaryCacheDto(
    @Schema(description = "타임딜 ID", example = "1") Long dealId,
    @Schema(description = "상품 ID", example = "1") Long productId,
    @Schema(description = "상품명", example = "신선한 사과") String title,
    @Schema(description = "상품 설명", example = "당도 높은 국산 사과입니다.") String description,
    @Schema(description = "원래 가격", example = "5000") int defaultPrice,
    @Schema(description = "할인된 가격", example = "3000") int discountedPrice,
    @Schema(description = "할인율 (%)", example = "40") int discountedPercentage,
    @Schema(description = "타임딜 재고 수량", example = "50") int stock,
    @Schema(description = "상품 이미지 URL", example = "https://example.com/apple.jpg") String imageUrl,
    @Schema(description = "타임딜 시작 시간", example = "2024-12-01T10:00:00+00:00")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime dealStartTime,
    @Schema(description = "타임딜 종료 시간", example = "2024-12-01T23:59:59+00:00")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime dealEndTime,
    @Schema(
            description = "상품 상태",
            example = "ACTIVE",
            allowableValues = {"ACTIVE", "SOLD_OUT"})
        String productStatus,
    @Schema(
            description = "타임딜 상태",
            example = "ONGOING",
            allowableValues = {"ONGOING", "UPCOMING"})
        String timeDealStatus) {}
