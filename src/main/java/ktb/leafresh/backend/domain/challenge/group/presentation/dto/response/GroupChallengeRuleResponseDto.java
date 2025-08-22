package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Builder
public record GroupChallengeRuleResponseDto(
    CertificationPeriod certificationPeriod, List<GroupChallengeExampleImageDto> exampleImages) {

  @Builder
  public record CertificationPeriod(
      OffsetDateTime startDate, OffsetDateTime endDate, LocalTime startTime, LocalTime endTime) {}

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
