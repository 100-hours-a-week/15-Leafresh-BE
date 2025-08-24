package ktb.leafresh.backend.domain.challenge.group.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "group_challenges",
    indexes = {@Index(name = "idx_group_challenges_deleted_at", columnList = "deleted_at")})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupChallenge extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  private GroupChallengeCategory category;

  @Builder.Default
  @OneToMany(mappedBy = "groupChallenge", cascade = CascadeType.ALL)
  private List<GroupChallengeExampleImage> exampleImages = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(nullable = false, length = 512)
  private String imageUrl;

  @Column(nullable = false, length = 100)
  private String title;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private Integer leafReward;

  @Column(nullable = false)
  private LocalDateTime startDate;

  @Column(nullable = false)
  private LocalDateTime endDate;

  @Column(nullable = false)
  private LocalTime verificationStartTime;

  @Column(nullable = false)
  private LocalTime verificationEndTime;

  @Column(nullable = false)
  private Integer maxParticipantCount;

  @Column(nullable = false)
  private Integer currentParticipantCount;

  @Column(nullable = false)
  private Boolean eventFlag;

  @PrePersist
  public void prePersist() {
    if (eventFlag == null) eventFlag = false;
  }

  public void addExampleImage(GroupChallengeExampleImage image) {
    this.exampleImages.add(image);
    if (image.getGroupChallenge() == null) {
      image.setGroupChallenge(this);
    }
  }

  public void updateInfo(
      String title,
      String description,
      String imageUrl,
      int maxParticipantCount,
      OffsetDateTime startDate,
      OffsetDateTime endDate,
      LocalTime verificationStart,
      LocalTime verificationEnd) {
    this.title = title;
    this.description = description;
    this.imageUrl = imageUrl;
    this.maxParticipantCount = maxParticipantCount;
    this.startDate = startDate.toLocalDateTime();
    this.endDate = endDate.toLocalDateTime();
    this.verificationStartTime = verificationStart;
    this.verificationEndTime = verificationEnd;
  }

  public void changeCategory(GroupChallengeCategory newCategory) {
    this.category = newCategory;
  }

  public void increaseParticipantCount() {
    this.currentParticipantCount++;
  }

  public void decreaseParticipantCount() {
    if (this.currentParticipantCount > 0) {
      this.currentParticipantCount--;
    }
  }

  public boolean isFull() {
    return this.currentParticipantCount >= this.maxParticipantCount;
  }

  public int getDurationInDays() {
    return (int) ChronoUnit.DAYS.between(this.startDate, this.endDate) + 1;
  }
}
