package ktb.leafresh.backend.domain.store.product.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

@Schema(description = "타임딜 수정 요청")
public record TimedealUpdateRequestDto(
    @Schema(description = "타임딜 시작 시간", example = "2024-12-01T10:00:00Z", required = true)
        @NotNull(message = "시작 시간은 필수입니다.")
        OffsetDateTime startTime,
    @Schema(description = "타임딜 종료 시간", example = "2024-12-01T23:59:59Z", required = true)
        @NotNull(message = "종료 시간은 필수입니다.")
        OffsetDateTime endTime,
    @Schema(description = "할인된 가격", example = "3000", minimum = "1", required = true)
        @NotNull(message = "할인 가격은 필수입니다.")
        @Min(value = 1, message = "할인 가격은 1 이상이어야 합니다.")
        Integer discountedPrice,
    @Schema(
            description = "할인율 (%)",
            example = "40",
            minimum = "1",
            maximum = "100",
            required = true)
        @NotNull(message = "할인율은 필수입니다.")
        @Min(value = 1, message = "할인율은 1 이상이어야 합니다.")
        Integer discountedPercentage,
    @Schema(description = "타임딜 재고 수량", example = "50", minimum = "1", required = true)
        @NotNull(message = "재고는 필수입니다.")
        @Min(value = 1, message = "재고는 1 이상이어야 합니다.")
        Integer stock) {}
