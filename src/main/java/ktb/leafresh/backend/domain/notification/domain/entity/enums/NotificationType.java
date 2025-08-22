package ktb.leafresh.backend.domain.notification.domain.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 유형")
public enum NotificationType {
  @Schema(description = "개인 알림")
  PERSONAL,
  @Schema(description = "그룹 알림")
  GROUP
}
