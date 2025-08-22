package ktb.leafresh.backend.domain.store.product.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record TimedealUpdateRequestDto(
    @NotNull(message = "시작 시간은 필수입니다.") OffsetDateTime startTime,
    @NotNull(message = "종료 시간은 필수입니다.") OffsetDateTime endTime,
    @NotNull(message = "할인 가격은 필수입니다.") @Min(value = 1, message = "할인 가격은 1 이상이어야 합니다.")
        Integer discountedPrice,
    @NotNull(message = "할인율은 필수입니다.") @Min(value = 1, message = "할인율은 1 이상이어야 합니다.")
        Integer discountedPercentage,
    @NotNull(message = "재고는 필수입니다.") @Min(value = 1, message = "재고는 1 이상이어야 합니다.") Integer stock) {}
