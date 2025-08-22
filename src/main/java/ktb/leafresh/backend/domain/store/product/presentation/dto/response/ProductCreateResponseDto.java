package ktb.leafresh.backend.domain.store.product.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 생성 응답")
public record ProductCreateResponseDto(
    @Schema(description = "생성된 상품의 ID", example = "1") Long id) {}
