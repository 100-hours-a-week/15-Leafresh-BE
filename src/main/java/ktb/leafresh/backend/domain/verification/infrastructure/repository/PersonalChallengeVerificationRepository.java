package ktb.leafresh.backend.domain.verification.infrastructure.repository;

import ktb.leafresh.backend.domain.verification.domain.entity.PersonalChallengeVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PersonalChallengeVerificationRepository extends JpaRepository<PersonalChallengeVerification, Long> {

    Optional<PersonalChallengeVerification> findTopByMemberIdAndPersonalChallengeIdAndCreatedAtBetween(
            Long memberId,
            Long challengeId,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );
}
