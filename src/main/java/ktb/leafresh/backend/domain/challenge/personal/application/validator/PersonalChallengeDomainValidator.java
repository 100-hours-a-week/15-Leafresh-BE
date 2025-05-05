package ktb.leafresh.backend.domain.challenge.personal.application.validator;

import ktb.leafresh.backend.domain.challenge.personal.infrastructure.repository.PersonalChallengeRepository;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PersonalChallengeDomainValidator {

    private final PersonalChallengeRepository repository;

    public void validate(DayOfWeek dayOfWeek) {
        int count = repository.countByDayOfWeek(dayOfWeek);
        if (count >= 3) {
            throw new CustomException(ChallengeErrorCode.EXCEEDS_DAILY_PERSONAL_CHALLENGE_LIMIT);
        }
    }
}
