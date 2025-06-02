package ktb.leafresh.backend.domain.store.product.infrastructure.cache;

import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.infrastructure.cache.dto.ProductSummaryCacheDto;

public class ProductSummaryCacheDtoMapper {

    public static ProductSummaryCacheDto from(Product product) {
        return new ProductSummaryCacheDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getImageUrl(),
                product.getPrice(),
                product.getStock(),
                product.getStatus().name(),
                product.getCreatedAt()
        );
    }
}
