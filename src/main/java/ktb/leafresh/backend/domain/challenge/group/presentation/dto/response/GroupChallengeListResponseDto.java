package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.global.util.pagination.CursorInfo;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.Builder;

import java.util.List;

@Schema(description = "단체 챌린지 목록 응답")
@Builder
public record GroupChallengeListResponseDto(
    @Schema(description = "단체 챌린지 목록") List<GroupChallengeSummaryResponseDto> groupChallenges,
    @Schema(description = "다음 페이지 존재 여부", example = "true") boolean hasNext,
    @Schema(description = "커서 페이지네이션 정보") CursorInfo cursorInfo) {

  public static GroupChallengeListResponseDto from(
      CursorPaginationResult<GroupChallengeSummaryResponseDto> result) {
    return new GroupChallengeListResponseDto(result.items(), result.hasNext(), result.cursorInfo());
  }
}
