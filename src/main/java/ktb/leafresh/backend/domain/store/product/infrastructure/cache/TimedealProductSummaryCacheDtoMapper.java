package ktb.leafresh.backend.domain.store.product.infrastructure.cache;

import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.dto.TimedealProductSummaryCacheDto;

import java.time.LocalDateTime;

public class TimedealProductSummaryCacheDtoMapper {

    public static TimedealProductSummaryCacheDto from(Product product, TimedealPolicy policy) {
        LocalDateTime now = LocalDateTime.now();
        String timeDealStatus = now.isBefore(policy.getStartTime()) ? "UPCOMING" : "ONGOING";

        return new TimedealProductSummaryCacheDto(
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
                product.getStatus().name(),
                timeDealStatus
        );
    }
}
