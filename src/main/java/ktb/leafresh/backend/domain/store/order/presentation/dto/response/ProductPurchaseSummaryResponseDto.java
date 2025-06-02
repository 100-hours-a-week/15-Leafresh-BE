package ktb.leafresh.backend.domain.store.order.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ktb.leafresh.backend.domain.store.order.domain.entity.ProductPurchase;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProductPurchaseSummaryResponseDto {

    private final Long id;

    private final ProductInfo product;

    private final int quantity;

    private final int price;

    private final String purchasedAt;

    @JsonIgnore
    private final String type;

    @Getter
    @Builder
    public static class ProductInfo {
        private final Long id;
        private final String title;
        private final String imageUrl;
    }

    public static ProductPurchaseSummaryResponseDto from(ProductPurchase entity) {
        return ProductPurchaseSummaryResponseDto.builder()
                .id(entity.getId())
                .product(ProductInfo.builder()
                        .id(entity.getProduct().getId())
                        .title(entity.getProduct().getName())
                        .imageUrl(entity.getProduct().getImageUrl())
                        .build())
                .quantity(entity.getQuantity())
                .price(entity.getPrice())
                .purchasedAt(entity.getPurchasedAt().toString())
                .type(entity.getType().name()) // JsonIgnore로 제외됨
                .build();
    }

    public static Long id(ProductPurchaseSummaryResponseDto dto) {
        return dto.getId();
    }

    public static LocalDateTime purchasedAt(ProductPurchaseSummaryResponseDto dto) {
        return LocalDateTime.parse(dto.getPurchasedAt());
    }
}
