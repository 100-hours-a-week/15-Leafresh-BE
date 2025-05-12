package ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;
import lombok.Builder;

import java.time.LocalTime;
import java.util.List;

@Builder
public record PersonalChallengeDetailResponseDto(
        Long id,
        String title,
        String description,
        String thumbnailUrl,
        DayOfWeek dayOfWeek,
        LocalTime verificationStartTime,
        LocalTime verificationEndTime,
        Integer leafReward,
        List<PersonalChallengeExampleImageDto> exampleImages,
        ChallengeStatus status
) {
    public static PersonalChallengeDetailResponseDto of(PersonalChallenge challenge,
                                                        List<PersonalChallengeExampleImageDto> images,
                                                        ChallengeStatus status) {
        return PersonalChallengeDetailResponseDto.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .thumbnailUrl(challenge.getImageUrl())
                .dayOfWeek(challenge.getDayOfWeek())
                .verificationStartTime(challenge.getVerificationStartTime())
                .verificationEndTime(challenge.getVerificationEndTime())
                .leafReward(challenge.getLeafReward())
                .exampleImages(images)
                .status(status)
                .build();
    }
}
