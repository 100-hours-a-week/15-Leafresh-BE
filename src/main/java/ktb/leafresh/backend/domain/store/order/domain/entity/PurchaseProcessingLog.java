package ktb.leafresh.backend.domain.store.order.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.domain.store.order.domain.entity.enums.PurchaseProcessingStatus;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import lombok.*;

@Entity
@Table(name = "purchase_processing_logs", indexes = {
        @Index(name = "idx_purchase_processing_logs_deleted", columnList = "deleted_at")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PurchaseProcessingLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseProcessingStatus status;

    @Column(columnDefinition = "TEXT")
    private String message;
}
