package ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;
import lombok.Builder;

import java.time.LocalTime;
import java.util.List;

@Builder
public record PersonalChallengeRuleResponseDto(
        CertificationPeriod certificationPeriod,
        List<PersonalChallengeExampleImageDto> exampleImages
) {

    @Builder
    public record CertificationPeriod(
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {}

    public static PersonalChallengeRuleResponseDto of(PersonalChallenge challenge,
                                                      List<PersonalChallengeExampleImageDto> images) {
        return PersonalChallengeRuleResponseDto.builder()
                .certificationPeriod(
                        CertificationPeriod.builder()
                                .dayOfWeek(challenge.getDayOfWeek())
                                .startTime(challenge.getVerificationStartTime())
                                .endTime(challenge.getVerificationEndTime())
                                .build()
                )
                .exampleImages(images)
                .build();
    }
}
