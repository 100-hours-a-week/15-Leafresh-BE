package ktb.leafresh.backend.domain.challenge.group.domain.entity;

import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;

import jakarta.persistence.*;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import ktb.leafresh.backend.global.common.entity.enums.ParticipantStatus;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "group_challenge_participant_records", indexes = {
        @Index(name = "idx_group_challenge_participant_records_deleted_at", columnList = "deleted_at")
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupChallengeParticipantRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_challenge_id", nullable = false)
    private GroupChallenge groupChallenge;

    @OneToMany(mappedBy = "participantRecord", cascade = CascadeType.ALL)
    private List<GroupChallengeVerification> verifications = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipantStatus status;

    public void changeStatus(ParticipantStatus newStatus) {
        this.status = newStatus;
    }

    public static GroupChallengeParticipantRecord create(Member member, GroupChallenge challenge, ParticipantStatus status) {
        return GroupChallengeParticipantRecord.builder()
                .member(member)
                .groupChallenge(challenge)
                .status(status)
                .build();
    }

    public boolean isActive() {
        return this.status == ParticipantStatus.ACTIVE;
    }
}
