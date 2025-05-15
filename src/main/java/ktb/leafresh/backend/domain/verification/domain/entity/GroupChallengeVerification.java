package ktb.leafresh.backend.domain.verification.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_challenge_verifications")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupChallengeVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_record_id", nullable = false)
    private GroupChallengeParticipantRecord participantRecord;

    @Column(nullable = false, length = 512)
    private String imageUrl;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChallengeStatus status;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(nullable = false)
    private boolean rewarded;

    @PrePersist
    public void prePersist() {
        this.rewarded = false;
    }

    public void markVerified(ChallengeStatus status) {
        this.status = status;
        this.verifiedAt = LocalDateTime.now();
    }

    public boolean isRewarded() {
        return this.rewarded;
    }

    public void markRewarded() {
        this.rewarded = true;
    }
}
