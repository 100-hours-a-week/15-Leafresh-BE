package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import ktb.leafresh.backend.global.util.pagination.CursorInfo;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.Builder;

import java.util.List;

@Builder
public record CreatedGroupChallengeListResponseDto(
    List<CreatedGroupChallengeSummaryResponseDto> groupChallenges,
    boolean hasNext,
    CursorInfo cursorInfo) {
  public static CreatedGroupChallengeListResponseDto from(
      CursorPaginationResult<CreatedGroupChallengeSummaryResponseDto> result) {
    return new CreatedGroupChallengeListResponseDto(
        result.items(), result.hasNext(), result.cursorInfo());
  }
}
