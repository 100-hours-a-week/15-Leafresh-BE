package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "단체 챌린지 참여 요약 정보")
@Builder
public record GroupChallengeParticipationSummaryDto(
    @Schema(description = "챌린지 ID", example = "1") Long id,
    @Schema(description = "챌린지 제목", example = "제로웨이스트 챌린지") String title,
    @Schema(description = "썸네일 이미지 URL", example = "https://leafresh.io/thumbnail.jpg")
        String thumbnailUrl,
    @Schema(description = "시작일", example = "2024-01-01T00:00:00+00:00") String startDate,
    @Schema(description = "종료일", example = "2024-01-31T23:59:59+00:00") String endDate,
    @Schema(description = "달성 현황") AchievementDto achievement,
    @Schema(description = "달성 기록 목록") List<AchievementRecordDto> achievementRecords,
    @JsonIgnore @Schema(hidden = true) OffsetDateTime createdAt) {

  @Schema(description = "달성 현황 정보")
  @Builder
  public record AchievementDto(
      @Schema(description = "성공 횟수", example = "15") Long success,
      @Schema(description = "총 시도 횟수", example = "20") Long total) {}

  @Schema(description = "달성 기록 정보")
  @Builder
  public record AchievementRecordDto(
      @Schema(description = "일차", example = "1") int day,
      @Schema(description = "상태", example = "SUCCESS") String status) {}

  public static GroupChallengeParticipationSummaryDto of(
      Long id,
      String title,
      String thumbnailUrl,
      OffsetDateTime startDate,
      OffsetDateTime endDate,
      Long success,
      Long total,
      List<AchievementRecordDto> achievementRecords,
      OffsetDateTime createdAt) {
    return GroupChallengeParticipationSummaryDto.builder()
        .id(id)
        .title(title)
        .thumbnailUrl(thumbnailUrl)
        .startDate(startDate.toString())
        .endDate(endDate.toString())
        .achievement(new AchievementDto(success, total))
        .achievementRecords(achievementRecords)
        .createdAt(createdAt)
        .build();
  }
}
