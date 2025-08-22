package ktb.leafresh.backend.domain.notification.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.global.util.pagination.CursorInfo;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.Builder;

import java.util.List;

@Schema(description = "알림 목록 응답")
@Builder
public record NotificationListResponse(
    @Schema(description = "알림 목록") List<NotificationSummaryResponse> notifications,
    @Schema(description = "다음 페이지 존재 여부", example = "true") boolean hasNext,
    @Schema(description = "커서 페이지네이션 정보") CursorInfo cursorInfo) {
  public static NotificationListResponse from(
      CursorPaginationResult<NotificationSummaryResponse> result) {
    return NotificationListResponse.builder()
        .notifications(result.items())
        .hasNext(result.hasNext())
        .cursorInfo(result.cursorInfo())
        .build();
  }
}
