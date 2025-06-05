package ktb.leafresh.backend.domain.store.product.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.domain.store.order.domain.entity.ProductPurchase;
import ktb.leafresh.backend.domain.store.order.domain.entity.PurchaseFailureLog;
import ktb.leafresh.backend.domain.store.order.domain.entity.PurchaseProcessingLog;
import ktb.leafresh.backend.domain.store.product.domain.entity.enums.ProductStatus;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<PurchaseFailureLog> failureLogs = new ArrayList<>();

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 40)
    private String description;

    @Column(nullable = false, length = 512)
    private String imageUrl;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductPurchase> purchases = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimedealPolicy> timedealPolicies = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<PurchaseProcessingLog> processingLogs = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (status == null) status = ProductStatus.ACTIVE;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void updatePrice(Integer price) {
        this.price = price;
    }

    public void updateStock(Integer stock) {
        this.stock = stock;
    }

    public void updateStatus(ProductStatus status) {
        this.status = status;
    }

    public Optional<TimedealPolicy> getActiveTimedealPolicy(LocalDateTime now) {
        LocalDateTime oneWeekLater = now.plusDays(7);
        return timedealPolicies.stream()
                .filter(policy ->
                        !policy.getStartTime().isAfter(oneWeekLater) &&  // 시작일이 일주일 내
                                !policy.getEndTime().isBefore(now)               // 끝나지 않음
                )
                .findFirst();
    }

    /**
     * 현재 시점 기준으로 구매 가능한 타임딜 정책 반환
     */
    public Optional<TimedealPolicy> getPurchasableTimedealPolicy(LocalDateTime now) {
        return timedealPolicies.stream()
                .filter(policy -> !policy.getStartTime().isAfter(now) && !policy.getEndTime().isBefore(now))
                .findFirst();
    }
}
