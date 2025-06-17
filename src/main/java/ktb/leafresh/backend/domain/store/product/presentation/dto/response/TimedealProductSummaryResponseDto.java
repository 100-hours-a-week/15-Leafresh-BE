package ktb.leafresh.backend.domain.store.product.presentation.dto.response;

import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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
) {
    public TimedealProductSummaryResponseDto(
            Long dealId,
            Long productId,
            String title,
            String description,
            int defaultPrice,
            int discountedPrice,
            int discountedPercentage,
            int stock,
            String imageUrl,
            LocalDateTime dealStartTime,
            LocalDateTime dealEndTime,
            String productStatus,
            String timeDealStatus
    ) {
        this(
                dealId,
                productId,
                title,
                description,
                defaultPrice,
                discountedPrice,
                discountedPercentage,
                stock,
                imageUrl,
                dealStartTime.atOffset(ZoneOffset.UTC),
                dealEndTime.atOffset(ZoneOffset.UTC),
                productStatus,
                timeDealStatus
        );
    }

    public static TimedealProductSummaryResponseDto from(TimedealPolicy policy) {
        var product = policy.getProduct();
        var now = LocalDateTime.now(ZoneOffset.UTC);

        String productStatus = switch (product.getStatus()) {
            case SOLD_OUT -> "SOLD_OUT";
            case ACTIVE -> "ACTIVE";
            default -> "INACTIVE";
        };

        String timeDealStatus = now.isBefore(policy.getStartTime()) ? "UPCOMING" : "ONGOING";

        return new TimedealProductSummaryResponseDto(
                policy.getId(),
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                policy.getDiscountedPrice(),
                policy.getDiscountedPercentage(),
                policy.getStock(),
                product.getImageUrl(),
                policy.getStartTime(),
                policy.getEndTime(),
                productStatus,
                timeDealStatus
        );
    }
}
