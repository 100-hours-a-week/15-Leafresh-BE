package ktb.leafresh.backend.domain.store.product.domain.factory;

import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.presentation.dto.request.ProductCreateRequestDto;
import org.springframework.stereotype.Component;

@Component
public class ProductFactory {
    public Product create(ProductCreateRequestDto dto) {
        return Product.builder()
                .name(dto.name())
                .description(dto.description())
                .imageUrl(dto.imageUrl())
                .price(dto.price())
                .stock(dto.stock())
                .status(dto.status())
                .build();
    }
}
