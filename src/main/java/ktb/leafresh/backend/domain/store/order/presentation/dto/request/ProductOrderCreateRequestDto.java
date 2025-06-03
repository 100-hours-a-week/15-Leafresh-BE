package ktb.leafresh.backend.domain.store.order.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProductOrderCreateRequestDto(
        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "최소 1개 이상 주문해야 합니다.")
        Integer quantity
) {}
