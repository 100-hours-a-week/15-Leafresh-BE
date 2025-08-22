package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "참여한 단체 챌린지 수 응답")
@Builder
public record GroupChallengeParticipationCountResponseDto(
    @Schema(description = "참여 챌린지 수 정보") GroupChallengeParticipationCountSummaryDto count) {

  public static GroupChallengeParticipationCountResponseDto from(
      GroupChallengeParticipationCountSummaryDto summary) {
    return new GroupChallengeParticipationCountResponseDto(summary);
  }
}
