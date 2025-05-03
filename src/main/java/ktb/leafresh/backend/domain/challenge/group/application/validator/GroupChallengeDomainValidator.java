package ktb.leafresh.backend.domain.challenge.group.application.validator;

import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeCreateRequestDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupChallengeDomainValidator {

    private final GroupChallengeCategoryRepository categoryRepository;

    public void validate(GroupChallengeCreateRequestDto dto) {
        if (dto.startDate().isAfter(dto.endDate())) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        }

        if (dto.verificationStartTime().isAfter(dto.verificationEndTime())) {
            throw new CustomException(ErrorCode.INVALID_VERIFICATION_TIME);
        }

        boolean exists = categoryRepository.findByName(dto.category()).isPresent();
        if (!exists) {
            throw new CustomException(ErrorCode.CHALLENGE_CATEGORY_NOT_FOUND);
        }
    }
}
