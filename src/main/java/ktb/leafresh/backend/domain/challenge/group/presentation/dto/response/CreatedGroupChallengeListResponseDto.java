package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.global.util.pagination.CursorInfo;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.Builder;

import java.util.List;

@Schema(description = "생성한 단체 챌린지 목록 응답")
@Builder
public record CreatedGroupChallengeListResponseDto(
    @Schema(description = "생성한 단체 챌린지 목록")
        List<CreatedGroupChallengeSummaryResponseDto> groupChallenges,
    @Schema(description = "다음 페이지 존재 여부", example = "true") boolean hasNext,
    @Schema(description = "커서 페이지네이션 정보") CursorInfo cursorInfo) {

  public static CreatedGroupChallengeListResponseDto from(
      CursorPaginationResult<CreatedGroupChallengeSummaryResponseDto> result) {
    return new CreatedGroupChallengeListResponseDto(
        result.items(), result.hasNext(), result.cursorInfo());
  }
}
