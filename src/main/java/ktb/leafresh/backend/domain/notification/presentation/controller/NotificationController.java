package ktb.leafresh.backend.domain.notification.presentation.controller;

import ktb.leafresh.backend.domain.notification.application.service.NotificationReadService;
import ktb.leafresh.backend.domain.notification.presentation.dto.response.NotificationListResponse;
import ktb.leafresh.backend.domain.notification.presentation.dto.response.NotificationSummaryResponse;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.exception.NotificationErrorCode;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CustomUserDetails;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/notifications")
public class NotificationController {

    private final NotificationReadService notificationReadService;

    @GetMapping
    public ResponseEntity<ApiResponse<NotificationListResponse>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) String cursorTimestamp,
            @RequestParam(defaultValue = "12") int size
    ) {
        if (userDetails == null) {
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
        }

        if ((cursorId == null) != (cursorTimestamp == null)) {
            throw new CustomException(GlobalErrorCode.INVALID_CURSOR);
        }

        try {
            Long memberId = userDetails.getMemberId();
            CursorPaginationResult<NotificationSummaryResponse> result =
                    notificationReadService.getNotifications(memberId, cursorId, cursorTimestamp, size);

            return ResponseEntity.ok(ApiResponse.success("알림 조회에 성공했습니다.", NotificationListResponse.from(result)));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(NotificationErrorCode.NOTIFICATION_READ_FAILED);
        }
    }

    @PatchMapping
    public ResponseEntity<Void> markAllNotificationsAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
        }

        try {
            Long memberId = userDetails.getMemberId();
            notificationReadService.markAllAsRead(memberId);
            return ResponseEntity.noContent().build();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(NotificationErrorCode.NOTIFICATION_MARK_READ_FAILED);
        }
    }
}
