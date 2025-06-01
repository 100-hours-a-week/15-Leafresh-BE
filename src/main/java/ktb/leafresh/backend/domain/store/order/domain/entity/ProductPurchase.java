package ktb.leafresh.backend.domain.store.order.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import ktb.leafresh.backend.domain.store.order.domain.entity.enums.PurchaseType;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_purchases")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductPurchase {

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
    private PurchaseType type;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "purchased_at", nullable = false)
    private LocalDateTime purchasedAt;
}
