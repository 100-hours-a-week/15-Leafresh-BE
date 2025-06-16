package ktb.leafresh.backend.domain.store.product.presentation.dto.response;

import java.time.OffsetDateTime;

public record TimedealProductSummaryResponseDto(
        Long dealId,
        Long productId,
        String title,
        String description,
        int defaultPrice,
        int discountedPrice,
        int discountedPercentage,
        int stock,
        String imageUrl,
        OffsetDateTime dealStartTime,
        OffsetDateTime dealEndTime,
        String productStatus,      // ACTIVE or SOLD_OUT
        String timeDealStatus      // ONGOING or UPCOMING
) {}
