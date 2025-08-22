package ktb.leafresh.backend.domain.store.product.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 수정 요청")
public record ProductUpdateRequestDto(
    @Schema(description = "상품명", example = "신선한 사과") String name,
    @Schema(description = "상품 설명", example = "당도 높은 국산 사과입니다.") String description,
    @Schema(description = "상품 이미지 URL", example = "https://example.com/apple.jpg") String imageUrl,
    @Schema(description = "상품 가격", example = "5000", minimum = "0") Integer price,
    @Schema(description = "재고 수량", example = "100", minimum = "0") Integer stock,
    @Schema(
            description = "상품 상태",
            example = "ACTIVE",
            allowableValues = {"ACTIVE", "SOLD_OUT", "HIDDEN", "INACTIVE", "DELETED"})
        String status) {}
