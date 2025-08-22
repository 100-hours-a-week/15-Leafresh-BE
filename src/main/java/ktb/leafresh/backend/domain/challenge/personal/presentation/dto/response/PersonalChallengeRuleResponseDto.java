package ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;
import lombok.Builder;

import java.time.LocalTime;
import java.util.List;

@Schema(description = "개인 챌린지 규칙 정보 응답")
@Builder
public record PersonalChallengeRuleResponseDto(
    @Schema(description = "인증 가능 기간") CertificationPeriod certificationPeriod,
    @Schema(description = "예시 이미지 목록") List<PersonalChallengeExampleImageDto> exampleImages) {

  @Schema(description = "인증 가능 기간 정보")
  @Builder
  public record CertificationPeriod(
      @Schema(description = "챌린지 실행 요일", example = "MONDAY") DayOfWeek dayOfWeek,
      @Schema(description = "인증 시작 시간", example = "09:00") LocalTime startTime,
      @Schema(description = "인증 종료 시간", example = "21:00") LocalTime endTime) {}

  public static PersonalChallengeRuleResponseDto of(
      PersonalChallenge challenge, List<PersonalChallengeExampleImageDto> images) {
    return PersonalChallengeRuleResponseDto.builder()
        .certificationPeriod(
            CertificationPeriod.builder()
                .dayOfWeek(challenge.getDayOfWeek())
                .startTime(challenge.getVerificationStartTime())
                .endTime(challenge.getVerificationEndTime())
                .build())
        .exampleImages(images)
        .build();
  }
}
