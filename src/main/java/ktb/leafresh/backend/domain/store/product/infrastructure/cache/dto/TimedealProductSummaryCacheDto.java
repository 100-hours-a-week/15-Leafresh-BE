package ktb.leafresh.backend.domain.store.product.infrastructure.cache.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

public record TimedealProductSummaryCacheDto(
    Long dealId,
    Long productId,
    String title,
    String description,
    int defaultPrice,
    int discountedPrice,
    int discountedPercentage,
    int stock,
    String imageUrl,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime dealStartTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime dealEndTime,
    String productStatus, // ACTIVE or SOLD_OUT
    String timeDealStatus // ONGOING or UPCOMING
    ) {}
