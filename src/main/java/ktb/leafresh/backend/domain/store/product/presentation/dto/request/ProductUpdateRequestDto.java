package ktb.leafresh.backend.domain.store.product.presentation.dto.request;

public record ProductUpdateRequestDto(
        String name,
        String description,
        String imageUrl,
        Integer price,
        Integer stock,
        String status // ACTIVE, SOLD_OUT, HIDDEN, INACTIVE, DELETED 중 하나
) {}
