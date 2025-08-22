package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Schema(description = "단체 챌린지 규칙 정보 응답")
@Builder
public record GroupChallengeRuleResponseDto(
    @Schema(description = "인증 가능 기간") CertificationPeriod certificationPeriod,
    @Schema(description = "예시 이미지 목록") List<GroupChallengeExampleImageDto> exampleImages) {

  @Schema(description = "인증 가능 기간 정보")
  @Builder
  public record CertificationPeriod(
      @Schema(description = "챌린지 시작일", example = "2024-01-01T00:00:00+00:00")
          OffsetDateTime startDate,
      @Schema(description = "챌린지 종료일", example = "2024-01-31T23:59:59+00:00")
          OffsetDateTime endDate,
      @Schema(description = "인증 시작 시간", example = "09:00") LocalTime startTime,
      @Schema(description = "인증 종료 시간", example = "21:00") LocalTime endTime) {}

  public static GroupChallengeRuleResponseDto of(
      GroupChallenge challenge, List<GroupChallengeExampleImageDto> images) {
    return GroupChallengeRuleResponseDto.builder()
        .certificationPeriod(
            CertificationPeriod.builder()
                .startDate(challenge.getStartDate().atOffset(ZoneOffset.UTC))
                .endDate(challenge.getEndDate().atOffset(ZoneOffset.UTC))
                .startTime(challenge.getVerificationStartTime())
                .endTime(challenge.getVerificationEndTime())
                .build())
        .exampleImages(images)
        .build();
  }
}
