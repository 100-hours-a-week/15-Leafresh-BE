package ktb.leafresh.backend.domain.verification.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "personal_challenge_verifications")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalChallengeVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_challenge_id", nullable = false)
    private PersonalChallenge personalChallenge;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

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
