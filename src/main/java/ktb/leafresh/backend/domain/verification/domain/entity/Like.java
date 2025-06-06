package ktb.leafresh.backend.domain.verification.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import lombok.*;

@Entity
@Table(
        name = "likes",
        indexes = @Index(name = "idx_like_deleted", columnList = "deleted_at"),
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_verification_member", columnNames = {"verification_id", "member_id"})
        }
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verification_id", nullable = false)
    private GroupChallengeVerification verification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public void restoreLike() {
        super.restore();
    }
}
