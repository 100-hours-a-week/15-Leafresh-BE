package ktb.leafresh.backend.domain.feedback.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_failure_logs")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FeedbackFailureLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 실패 발생한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 실패 사유
    @Column(columnDefinition = "TEXT")
    private String reason;

    // 요청 본문 백업 (JSON)
    @Column(name = "request_body", columnDefinition = "JSON")
    private String requestBody;

    // 발생 시각
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}
