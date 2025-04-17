package ktb.leafresh.backend.domain.store.product.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.domain.store.order.domain.entity.ProductPurchase;
import ktb.leafresh.backend.domain.store.product.domain.entity.enums.ProductStatus;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = @Index(name = "idx_product_deleted", columnList = "deleted_at"))
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false, length = 512)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductPurchase> purchases = new ArrayList<>();

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)
    private TimedealPolicy timedealPolicy;

    @PrePersist
    public void prePersist() {
        if (status == null) status = ProductStatus.AVAILABLE;
    }
}
