package ktb.leafresh.backend.domain.challenge.group.presentation.dto.query;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
public class GroupChallengeParticipationDto {
  private Long id;
  private String title;
  private String thumbnailUrl;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private Long success;
  private Long total;
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
