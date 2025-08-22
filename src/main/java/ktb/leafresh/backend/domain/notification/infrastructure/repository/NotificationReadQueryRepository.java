package ktb.leafresh.backend.domain.notification.infrastructure.repository;

import ktb.leafresh.backend.domain.notification.domain.entity.Notification;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationReadQueryRepository {
  List<Notification> findAllWithCursorAndMemberId(
      LocalDateTime cursorTimestamp, Long cursorId, int size, Long memberId);
}
