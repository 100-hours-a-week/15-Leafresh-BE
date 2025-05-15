package ktb.leafresh.backend.domain.member.domain.entity;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.community.comment.domain.entity.Comment;
import ktb.leafresh.backend.domain.community.like.domain.entity.Like;
import ktb.leafresh.backend.domain.community.post.domain.entity.Post;
import ktb.leafresh.backend.domain.feedback.domain.entity.Feedback;
import ktb.leafresh.backend.domain.member.domain.entity.enums.LoginType;
import ktb.leafresh.backend.domain.member.domain.entity.enums.Role;

import jakarta.persistence.*;
import ktb.leafresh.backend.domain.auth.domain.entity.OAuth;
import ktb.leafresh.backend.domain.notification.domain.entity.Notification;
import ktb.leafresh.backend.domain.store.order.domain.entity.ProductPurchase;
import ktb.leafresh.backend.domain.verification.domain.entity.PersonalChallengeVerification;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "members", indexes = {
        @Index(name = "idx_member_deleted_at", columnList = "deleted_at")
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tree_level_id", nullable = false)
    private TreeLevel treeLevel;

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<OAuth> auths = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberBadge> memberBadges = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<GroupChallenge> groupChallenges = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<GroupChallengeParticipantRecord> groupChallengeParticipantRecords = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<PersonalChallengeVerification> personalChallengeVerifications = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<ProductPurchase> productPurchases = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Feedback> feedbacks = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoginType loginType;

    @Column(nullable = false, unique = true, length = 128)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    @Column(nullable = false, length = 512)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private Boolean activated;

    @Column(nullable = false)
    private Integer totalLeafPoints;

    @Column(nullable = false)
    private Integer currentLeafPoints;

    @Column(name = "last_login_rewarded_at")
    private LocalDateTime lastLoginRewardedAt;

    @PrePersist
    public void prePersist() {
        if (activated == null) activated = true;
        if (totalLeafPoints == null) totalLeafPoints = 0;
        if (currentLeafPoints == null) currentLeafPoints = 0;
    }

    public void addLeafPoints(int amount) {
        this.currentLeafPoints += amount;
        this.totalLeafPoints += amount;
    }

    public boolean hasReceivedLoginRewardToday() {
        return lastLoginRewardedAt != null &&
                lastLoginRewardedAt.toLocalDate().isEqual(LocalDate.now());
    }

    public void updateLastLoginRewardedAt() {
        this.lastLoginRewardedAt = LocalDateTime.now();
    }
}
