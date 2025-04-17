package ktb.leafresh.backend.domain.store.order.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import ktb.leafresh.backend.domain.store.order.domain.entity.enums.PurchaseType;
import lombok.*;

@Entity
@Table(name = "product_purchases", indexes = @Index(name = "idx_purchase_deleted", columnList = "deleted_at"))
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductPurchase extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseType purchaseType;

    @Column(nullable = false)
    private Integer purchasePrice;
}
