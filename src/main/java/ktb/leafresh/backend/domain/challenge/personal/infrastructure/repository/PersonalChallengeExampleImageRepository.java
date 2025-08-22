package ktb.leafresh.backend.domain.challenge.personal.infrastructure.repository;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallengeExampleImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalChallengeExampleImageRepository
    extends JpaRepository<PersonalChallengeExampleImage, Long> {}
