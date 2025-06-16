package ktb.leafresh.backend.domain.store.product.presentation.dto.request;

import java.time.OffsetDateTime;

public record TimedealUpdateRequestDto(
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        Integer discountedPrice,
        Integer discountedPercentage,
        Integer stock
) {}
