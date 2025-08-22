package ktb.leafresh.backend.domain.store.order.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "상품 주문 생성 요청")
public record ProductOrderCreateRequestDto(
    @Schema(description = "주문 수량", example = "2", minimum = "1", required = true)
        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "최소 1개 이상 주문해야 합니다.")
        Integer quantity) {}
