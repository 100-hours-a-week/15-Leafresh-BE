package ktb.leafresh.backend.domain.verification.infrastructure.repository;

public interface PersonalChallengeVerificationCustomRepository {
  int countConsecutiveSuccessDays(Long memberId);
}
