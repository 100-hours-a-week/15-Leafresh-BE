package ktb.leafresh.backend.domain.notification.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import ktb.leafresh.backend.domain.notification.application.service.NotificationReadService;
import ktb.leafresh.backend.domain.notification.presentation.dto.response.NotificationListResponse;
import ktb.leafresh.backend.domain.notification.presentation.dto.response.NotificationSummaryResponse;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CurrentMemberId;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification", description = "알림 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/notifications")
@Validated
public class NotificationController {

  private final NotificationReadService notificationReadService;

  @GetMapping
  @Operation(summary = "알림 목록 조회", description = "사용자의 알림을 커서 기반 페이지네이션으로 조회합니다.")
  public ResponseEntity<ApiResponse<NotificationListResponse>> getNotifications(
      @CurrentMemberId Long memberId,
      @Parameter(description = "커서 ID") @RequestParam(required = false) Long cursorId,
      @Parameter(description = "커서 타임스탬프") @RequestParam(required = false) String cursorTimestamp,
      @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "12") @Min(1) @Max(50) int size) {

    CursorPaginationResult<NotificationSummaryResponse> result =
        notificationReadService.getNotifications(memberId, cursorId, cursorTimestamp, size);

    return ResponseEntity.ok(
        ApiResponse.success("알림 조회에 성공했습니다.", NotificationListResponse.from(result)));
  }

  @PatchMapping
  @Operation(summary = "모든 알림 읽음 처리", description = "사용자의 모든 알림을 읽음 상태로 변경합니다.")
  public ResponseEntity<Void> markAllNotificationsAsRead(@CurrentMemberId Long memberId) {

    notificationReadService.markAllAsRead(memberId);
    return ResponseEntity.noContent().build();
  }
}
