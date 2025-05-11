package ktb.leafresh.backend.domain.notification.presentation.dto.response;

import ktb.leafresh.backend.domain.notification.domain.entity.Notification;
import ktb.leafresh.backend.domain.notification.domain.entity.enums.NotificationType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NotificationDto(
        Long id,
        String title,
        String content,
        LocalDateTime createdAt,
        boolean isRead,
        NotificationType type,
        Long challengeId
) {
    public static NotificationDto from(Notification entity) {
        return NotificationDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .isRead(entity.isStatus())
                .type(entity.getType())
                .challengeId(entity.getChallengeId())
                .build();
    }

    public LocalDateTime createdAt() {
        return this.createdAt;
    }

    public Long id() {
        return this.id;
    }
}
