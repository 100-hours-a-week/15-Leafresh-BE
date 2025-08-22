package ktb.leafresh.backend.domain.notification.application.service;

import ktb.leafresh.backend.domain.notification.domain.entity.Notification;
import ktb.leafresh.backend.domain.notification.infrastructure.repository.NotificationReadQueryRepository;
import ktb.leafresh.backend.domain.notification.infrastructure.repository.NotificationRepository;
import ktb.leafresh.backend.domain.notification.presentation.dto.response.NotificationSummaryResponse;
import ktb.leafresh.backend.global.util.pagination.CursorConditionUtils;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationHelper;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationReadService {

  private final NotificationReadQueryRepository notificationReadQueryRepository;
  private final NotificationRepository notificationRepository;

  @Transactional(readOnly = true)
  public CursorPaginationResult<NotificationSummaryResponse> getNotifications(
      Long memberId, Long cursorId, String cursorTimestamp, int size) {
    LocalDateTime parsedTimestamp = CursorConditionUtils.parseTimestamp(cursorTimestamp);

    List<Notification> notifications =
        notificationReadQueryRepository.findAllWithCursorAndMemberId(
            parsedTimestamp, cursorId, size + 1, memberId);

    return CursorPaginationHelper.paginateWithTimestamp(
        notifications,
        size,
        NotificationSummaryResponse::from,
        NotificationSummaryResponse::id,
        dto -> dto.createdAt().toLocalDateTime());
  }

  @Transactional
  public void markAllAsRead(Long memberId) {
    List<Notification> unreadNotifications =
        notificationRepository.findByMemberIdAndStatusFalse(memberId);

    if (unreadNotifications.isEmpty()) {
      log.info("[알림 읽음 처리] memberId={} - 읽지 않은 알림 없음", memberId);
      return;
    }

    log.info("[알림 읽음 처리 시작] memberId={}, 읽지 않은 알림 수={}", memberId, unreadNotifications.size());

    unreadNotifications.forEach(Notification::markAsRead);

    log.info("[알림 읽음 처리 완료] memberId={}", memberId);
  }
}
