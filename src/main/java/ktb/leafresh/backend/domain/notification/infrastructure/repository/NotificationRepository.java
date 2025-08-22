package ktb.leafresh.backend.domain.notification.infrastructure.repository;

import ktb.leafresh.backend.domain.notification.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  List<Notification> findByMemberIdAndStatusFalse(Long memberId);
}
