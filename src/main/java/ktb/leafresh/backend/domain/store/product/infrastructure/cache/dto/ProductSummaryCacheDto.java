package ktb.leafresh.backend.domain.store.product.infrastructure.cache.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "상품 요약 캐시 데이터")
public record ProductSummaryCacheDto(
    @Schema(description = "상품 ID", example = "1") Long id,
    @Schema(description = "상품명", example = "신선한 사과") String title,
    @Schema(description = "상품 설명", example = "당도 높은 국산 사과입니다.") String description,
    @Schema(description = "상품 이미지 URL", example = "https://example.com/apple.jpg") String imageUrl,
    @Schema(description = "상품 가격", example = "5000") int price,
    @Schema(description = "재고 수량", example = "100") int stock,
    @Schema(description = "상품 상태", example = "ACTIVE") String status,
    @Schema(description = "생성 시간", example = "2024-12-01T10:00:00+00:00")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime createdAt) {}
