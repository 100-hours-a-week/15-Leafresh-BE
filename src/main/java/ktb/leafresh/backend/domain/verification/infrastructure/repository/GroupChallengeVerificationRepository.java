package ktb.leafresh.backend.domain.verification.infrastructure.repository;

import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GroupChallengeVerificationRepository extends JpaRepository<GroupChallengeVerification, Long> {

    /**
     * 특정 회원이 특정 단체 챌린지에 대해 마지막으로 인증한 기록을 조회
     */
    Optional<GroupChallengeVerification> findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdOrderByCreatedAtDesc(
            Long memberId,
            Long challengeId
    );

    /**
     * 단체 챌린지 상세 페이지에 보여줄 최신 인증 이미지 9개 조회
     */
    List<GroupChallengeVerification> findTop9ByParticipantRecord_GroupChallenge_IdOrderByCreatedAtDesc(Long challengeId);

    Optional<GroupChallengeVerification> findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdAndCreatedAtBetween(
            Long memberId,
            Long challengeId,
            LocalDateTime start,
            LocalDateTime end
    );
}
