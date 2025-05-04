package ktb.leafresh.backend.domain.challenge.group.application.validator;

import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeCreateRequestDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class GroupChallengeDomainValidator {

    private final GroupChallengeCategoryRepository categoryRepository;

    public void validate(GroupChallengeCreateRequestDto dto) {
        if (!dto.endDate().isAfter(dto.startDate())) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        }

        if (ChronoUnit.DAYS.between(dto.startDate(), dto.endDate()) < 1) {
            throw new CustomException(ErrorCode.CHALLENGE_DURATION_TOO_SHORT);
        }

        if (!dto.verificationEndTime().isAfter(dto.verificationStartTime())) {
            throw new CustomException(ErrorCode.INVALID_VERIFICATION_TIME);
        }

        if (Duration.between(dto.verificationStartTime(), dto.verificationEndTime()).toMinutes() < 10) {
            throw new CustomException(ErrorCode.VERIFICATION_DURATION_TOO_SHORT);
        }

        boolean exists = categoryRepository.findByName(dto.category()).isPresent();
        if (!exists) {
            throw new CustomException(ErrorCode.CHALLENGE_CATEGORY_NOT_FOUND);
        }
    }
}
