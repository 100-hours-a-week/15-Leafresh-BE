package ktb.leafresh.backend.domain.member.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_badges")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberBadge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    @Column(name = "acquired_at", nullable = false)
    private LocalDateTime acquiredAt;

    public static MemberBadge of(Member member, Badge badge) {
        return MemberBadge.builder()
                .member(member)
                .badge(badge)
                .acquiredAt(LocalDateTime.now())
                .build();
    }
}
