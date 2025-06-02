package ktb.leafresh.backend.domain.store.product.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ktb.leafresh.backend.domain.store.product.domain.entity.enums.ProductStatus;

public record ProductCreateRequestDto(
        @NotBlank(message = "상품명은 필수 항목입니다.") String name,
        @NotBlank(message = "설명은 필수 항목입니다.") String description,
        @NotBlank(message = "상품 이미지는 필수 항목입니다.") String imageUrl,
        @NotNull(message = "가격은 필수 항목입니다.") Integer price,
        @NotNull(message = "재고는 필수 항목입니다.") Integer stock,
        @NotNull(message = "상태는 필수 항목입니다.") ProductStatus status
) {}
