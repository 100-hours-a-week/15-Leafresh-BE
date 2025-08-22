package ktb.leafresh.backend.domain.feedback.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "feedbacks",
    indexes = @Index(name = "idx_feedback_deleted", columnList = "deleted_at"))
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feedback extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "week_monday", nullable = false)
  private LocalDateTime weekMonday;

  public static Feedback of(Member member, String content, LocalDateTime weekMonday) {
    return Feedback.builder().member(member).content(content).weekMonday(weekMonday).build();
  }
}
