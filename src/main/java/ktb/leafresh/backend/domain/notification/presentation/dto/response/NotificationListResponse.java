package ktb.leafresh.backend.domain.notification.presentation.dto.response;

import ktb.leafresh.backend.global.util.pagination.CursorInfo;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.Builder;

import java.util.List;

@Builder
public record NotificationListResponse(
        List<NotificationSummaryResponse> notifications,
        boolean hasNext,
        CursorInfo cursorInfo
) {
    public static NotificationListResponse from(CursorPaginationResult<NotificationSummaryResponse> result) {
        return NotificationListResponse.builder()
                .notifications(result.items())
                .hasNext(result.hasNext())
                .cursorInfo(result.cursorInfo())
                .build();
    }
}
