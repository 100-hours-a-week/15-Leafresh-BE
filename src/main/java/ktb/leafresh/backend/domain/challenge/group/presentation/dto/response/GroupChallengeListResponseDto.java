package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import ktb.leafresh.backend.global.util.pagination.CursorInfo;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.Builder;

import java.util.List;

@Builder
public record GroupChallengeListResponseDto(
        List<GroupChallengeSummaryResponseDto> groupChallenges,
        boolean hasNext,
        CursorInfo cursorInfo
) {
    public static GroupChallengeListResponseDto from(CursorPaginationResult<GroupChallengeSummaryResponseDto> result) {
        return new GroupChallengeListResponseDto(
                result.items(),
                result.hasNext(),
                result.cursorInfo()
        );
    }
}
