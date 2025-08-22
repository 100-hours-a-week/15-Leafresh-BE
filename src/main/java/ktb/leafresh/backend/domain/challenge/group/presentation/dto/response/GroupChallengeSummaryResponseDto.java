package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.service.GroupChallengeRemainingDayCalculator;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Builder
public record GroupChallengeSummaryResponseDto(
    Long id,
    String title,
    String category,
    String description,
    String thumbnailUrl,
    int leafReward,
    OffsetDateTime startDate,
    OffsetDateTime endDate,
    int remainingDay,
    int currentParticipantCount,
    LocalDateTime createdAt) {
  public static GroupChallengeSummaryResponseDto from(GroupChallenge entity) {
    int remainingDay =
        GroupChallengeRemainingDayCalculator.calculate(entity.getStartDate().toLocalDate());

    return GroupChallengeSummaryResponseDto.builder()
        .id(entity.getId())
        .title(entity.getTitle())
        .category(entity.getCategory().getName())
        .description(entity.getDescription())
        .thumbnailUrl(entity.getImageUrl())
        .leafReward(entity.getLeafReward())
        .startDate(entity.getStartDate().atOffset(ZoneOffset.UTC))
        .endDate(entity.getEndDate().atOffset(ZoneOffset.UTC))
        .currentParticipantCount(entity.getCurrentParticipantCount())
        .createdAt(entity.getCreatedAt())
        .remainingDay(remainingDay)
        .build();
  }

  public static List<GroupChallengeSummaryResponseDto> fromEntities(List<GroupChallenge> entities) {
    return entities.stream().map(GroupChallengeSummaryResponseDto::from).toList();
  }

  public LocalDateTime createdAt() {
    return this.createdAt;
  }
}
