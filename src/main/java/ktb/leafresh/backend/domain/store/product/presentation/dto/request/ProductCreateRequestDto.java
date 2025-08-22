package ktb.leafresh.backend.domain.store.product.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ktb.leafresh.backend.domain.store.product.domain.entity.enums.ProductStatus;

@Schema(description = "상품 생성 요청")
public record ProductCreateRequestDto(
    @Schema(description = "상품명", example = "신선한 사과", required = true)
        @NotBlank(message = "상품명은 필수 항목입니다.")
        String name,
    @Schema(description = "상품 설명", example = "당도 높은 국산 사과입니다.", required = true)
        @NotBlank(message = "설명은 필수 항목입니다.")
        String description,
    @Schema(description = "상품 이미지 URL", example = "https://example.com/apple.jpg", required = true)
        @NotBlank(message = "상품 이미지는 필수 항목입니다.")
        String imageUrl,
    @Schema(description = "상품 가격", example = "5000", minimum = "0", required = true)
        @NotNull(message = "가격은 필수 항목입니다.")
        Integer price,
    @Schema(description = "재고 수량", example = "100", minimum = "0", required = true)
        @NotNull(message = "재고는 필수 항목입니다.")
        Integer stock,
    @Schema(
            description = "상품 상태",
            example = "ACTIVE",
            required = true,
            allowableValues = {"ACTIVE", "SOLD_OUT", "HIDDEN", "INACTIVE", "DELETED"})
        @NotNull(message = "상태는 필수 항목입니다.")
        ProductStatus status) {}
