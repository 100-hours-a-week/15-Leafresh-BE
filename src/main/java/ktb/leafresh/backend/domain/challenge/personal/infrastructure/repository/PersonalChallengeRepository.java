package ktb.leafresh.backend.domain.challenge.personal.infrastructure.repository;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalChallengeRepository extends JpaRepository<PersonalChallenge, Long> {
    int countByDayOfWeek(DayOfWeek dayOfWeek);
}
