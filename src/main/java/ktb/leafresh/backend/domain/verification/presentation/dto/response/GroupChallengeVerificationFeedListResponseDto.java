package ktb.leafresh.backend.domain.verification.presentation.dto.response;

import ktb.leafresh.backend.global.util.pagination.CursorInfo;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.Builder;

import java.util.List;

@Builder
public record GroupChallengeVerificationFeedListResponseDto(
        List<GroupChallengeVerificationFeedSummaryDto> verifications,
        boolean hasNext,
        CursorInfo cursorInfo
) {
    public static GroupChallengeVerificationFeedListResponseDto from(CursorPaginationResult<GroupChallengeVerificationFeedSummaryDto> result) {
        return new GroupChallengeVerificationFeedListResponseDto(
                result.items(),
                result.hasNext(),
                result.cursorInfo()
        );
    }
}
