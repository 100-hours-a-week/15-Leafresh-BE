package ktb.leafresh.backend.domain.challenge.group.application.validator;

import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeCreateRequestDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeUpdateRequestDto;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class GroupChallengeDomainValidator {

    private final GroupChallengeCategoryRepository categoryRepository;

    public void validate(GroupChallengeCreateRequestDto dto) {
        validateCommon(dto.startDate(), dto.endDate(), dto.verificationStartTime(), dto.verificationEndTime(), dto.category());

        if (dto.exampleImages() == null || dto.exampleImages().isEmpty()) {
            throw new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_REQUIRES_SUCCESS_IMAGE);
        }

        long successImageCount = dto.exampleImages().stream()
                .filter(image -> image.type() == ExampleImageType.SUCCESS)
                .count();
        if (successImageCount == 0) {
            throw new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_REQUIRES_SUCCESS_IMAGE);
        }
    }

    public void validate(GroupChallengeUpdateRequestDto dto) {
        validateCommon(dto.startDate(), dto.endDate(), dto.verificationStartTime(), dto.verificationEndTime(), dto.category());

        // 신규 추가 이미지 중 성공 타입만 카운트
        long successImageCount = dto.exampleImages().newImages().stream()
                .filter(image -> image.type() == ExampleImageType.SUCCESS)
                .count();

        // 기존 유지 이미지가 하나라도 있으면 OK
        boolean hasExistingSuccessImage = dto.exampleImages().keep().stream()
                .anyMatch(image -> {
                    // ID만 있기 때문에 실제 타입 확인이 불가 → 서비스단에서 보완 필요
                    // 여기선 신규 + 기존 합쳐 최소 1개로만 판단
                    return true; // assume success 이미지 포함 가능성
                });

        if (successImageCount == 0 && !hasExistingSuccessImage) {
            throw new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_REQUIRES_SUCCESS_IMAGE);
        }
    }

    private void validateCommon(
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            LocalTime verificationStartTime,
            LocalTime verificationEndTime,
            String categoryName
    ) {
        if (!endDate.isAfter(startDate)) {
            throw new CustomException(ChallengeErrorCode.INVALID_DATE_RANGE);
        }

        if (ChronoUnit.DAYS.between(startDate, endDate) < 1) {
            throw new CustomException(ChallengeErrorCode.CHALLENGE_DURATION_TOO_SHORT);
        }

        if (!verificationEndTime.isAfter(verificationStartTime)) {
            throw new CustomException(ChallengeErrorCode.INVALID_VERIFICATION_TIME);
        }

        if (Duration.between(verificationStartTime, verificationEndTime).toMinutes() < 10) {
            throw new CustomException(ChallengeErrorCode.VERIFICATION_DURATION_TOO_SHORT);
        }

        boolean exists = categoryRepository.findByName(categoryName).isPresent();
        if (!exists) {
            throw new CustomException(ChallengeErrorCode.CHALLENGE_CATEGORY_NOT_FOUND);
        }
    }
}
