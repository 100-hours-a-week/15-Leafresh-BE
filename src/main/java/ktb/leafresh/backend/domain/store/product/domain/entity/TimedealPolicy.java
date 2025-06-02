package ktb.leafresh.backend.domain.store.product.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "timedeal_policies", indexes = @Index(name = "idx_timedeal_deleted", columnList = "deleted_at"))
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimedealPolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer discountedPrice;

    @Column(nullable = false)
    private Integer discountedPercentage;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    public void updateTime(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void updatePriceAndPercent(Integer discountedPrice, Integer discountedPercentage) {
        if (discountedPrice != null) this.discountedPrice = discountedPrice;
        if (discountedPercentage != null) this.discountedPercentage = discountedPercentage;
    }
}
