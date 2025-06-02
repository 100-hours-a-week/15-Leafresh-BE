package ktb.leafresh.backend.domain.store.product.presentation.dto.request;

import java.time.LocalDateTime;

public record TimedealUpdateRequestDto(
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer discountedPrice,
        Integer discountedPercentage
) {}
