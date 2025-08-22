package ktb.leafresh.backend.domain.verification.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

  @OneToMany(mappedBy = "verification", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Like> likes = new ArrayList<>();

  @OneToMany(mappedBy = "verification", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Comment> comments = new ArrayList<>();

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

  @Column(name = "view_count", nullable = false)
  @ColumnDefault("0")
  private int viewCount;

  @Column(name = "like_count", nullable = false)
  @ColumnDefault("0")
  private int likeCount;

  @Column(name = "comment_count", nullable = false)
  @ColumnDefault("0")
  private int commentCount;

  @PrePersist
  public void prePersist() {
    this.rewarded = false;
    this.viewCount = 0;
    this.likeCount = 0;
    this.commentCount = 0;
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
