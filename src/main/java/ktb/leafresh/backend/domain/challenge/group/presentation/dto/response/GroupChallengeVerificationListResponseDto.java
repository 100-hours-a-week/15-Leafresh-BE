package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import ktb.leafresh.backend.global.util.pagination.CursorInfo;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.Builder;

import java.util.List;

@Builder
public record GroupChallengeVerificationListResponseDto(
        List<GroupChallengeVerificationSummaryDto> verifications,
        boolean hasNext,
        CursorInfo cursorInfo
) {
    public static GroupChallengeVerificationListResponseDto from(CursorPaginationResult<GroupChallengeVerificationSummaryDto> result) {
        return new GroupChallengeVerificationListResponseDto(
                result.items(),
                result.hasNext(),
                result.cursorInfo()
        );
    }
}
