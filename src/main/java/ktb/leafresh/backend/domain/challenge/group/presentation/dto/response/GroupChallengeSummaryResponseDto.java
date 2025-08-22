package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.service.GroupChallengeRemainingDayCalculator;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Schema(description = "단체 챌린지 요약 정보")
@Builder
public record GroupChallengeSummaryResponseDto(
    @Schema(description = "챌린지 ID", example = "1") Long id,
    @Schema(description = "챌린지 제목", example = "제로웨이스트 챌린지") String title,
    @Schema(description = "카테고리", example = "ZERO_WASTE") String category,
    @Schema(description = "챌린지 설명", example = "환경을 위한 제로웨이스트 챌린지에 참여해보세요!") String description,
    @Schema(description = "썸네일 이미지 URL", example = "https://leafresh.io/thumbnail.jpg")
        String thumbnailUrl,
    @Schema(description = "리프 보상 포인트", example = "20") int leafReward,
    @Schema(description = "챌린지 시작일", example = "2024-01-01T00:00:00+00:00")
        OffsetDateTime startDate,
    @Schema(description = "챌린지 종료일", example = "2024-01-31T23:59:59+00:00") OffsetDateTime endDate,
    @Schema(description = "시작까지 남은 일수", example = "5") int remainingDay,
    @Schema(description = "현재 참여 인원", example = "25") int currentParticipantCount,
    @Schema(description = "생성일시", example = "2024-01-01T10:00:00") LocalDateTime createdAt) {

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
