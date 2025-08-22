package ktb.leafresh.backend.domain.challenge.group.presentation.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Schema(description = "단체 챌린지 참여 정보 쿼리 DTO")
@Getter
@NoArgsConstructor
public class GroupChallengeParticipationDto {
  @Schema(description = "챌린지 ID", example = "1")
  private Long id;

  @Schema(description = "챌린지 제목", example = "제로웨이스트 챌린지")
  private String title;

  @Schema(description = "썸네일 이미지 URL", example = "https://leafresh.io/thumbnail.jpg")
  private String thumbnailUrl;

  @Schema(description = "챌린지 시작일", example = "2024-01-01T00:00:00")
  private LocalDateTime startDate;

  @Schema(description = "챌린지 종료일", example = "2024-01-31T23:59:59")
  private LocalDateTime endDate;

  @Schema(description = "성공 횟수", example = "15")
  private Long success;

  @Schema(description = "총 시도 횟수", example = "20")
  private Long total;

  @Schema(description = "참여 시작일", example = "2024-01-01T10:00:00")
  private LocalDateTime createdAt;

  public GroupChallengeParticipationDto(
      Long id,
      String title,
      String thumbnailUrl,
      LocalDateTime startDate,
      LocalDateTime endDate,
      Long success,
      Long total,
      LocalDateTime createdAt) {
    this.id = id;
    this.title = title;
    this.thumbnailUrl = thumbnailUrl;
    this.startDate = startDate;
    this.endDate = endDate;
    this.success = success;
    this.total = total;
    this.createdAt = createdAt;
  }
}
