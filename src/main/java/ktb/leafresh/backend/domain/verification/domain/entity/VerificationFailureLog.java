package ktb.leafresh.backend.domain.verification.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeType;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_failure_logs")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class VerificationFailureLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 실패 발생한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 챌린지 유형 (PERSONAL / GROUP)
    @Enumerated(EnumType.STRING)
    @Column(name = "challenge_type", nullable = false)
    private ChallengeType challengeType;

    // 챌린지 ID
    @Column(name = "challenge_id", nullable = false)
    private Long challengeId;

    // 인증 ID (optional: null 허용 가능)
    @Column(name = "verification_id")
    private Long verificationId;

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
