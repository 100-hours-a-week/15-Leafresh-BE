package ktb.leafresh.backend.domain.store.order.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import lombok.*;

@Entity
@Table(
        name = "purchase_idempotency_keys",
        uniqueConstraints = @UniqueConstraint(name = "uk_member_id_key", columnNames = {"member_id", "idempotency_key"}),
        indexes = @Index(name = "idx_purchase_idempotency_key_deleted", columnList = "deleted_at")
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PurchaseIdempotencyKey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 중복 방지 키 (e.g. 클라이언트가 생성한 UUID)
    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;
}
