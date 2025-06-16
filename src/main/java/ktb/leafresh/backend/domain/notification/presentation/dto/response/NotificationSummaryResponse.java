package ktb.leafresh.backend.domain.notification.presentation.dto.response;

import ktb.leafresh.backend.domain.notification.domain.entity.Notification;
import ktb.leafresh.backend.domain.notification.domain.entity.enums.NotificationType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Builder
public record NotificationSummaryResponse(
        Long id,
        String title,
        String content,
        OffsetDateTime createdAt,
        boolean isRead,
        NotificationType type,
        String imageUrl,
        Long challengeId
) {
    public static NotificationSummaryResponse from(Notification entity) {
        return NotificationSummaryResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt().atOffset(ZoneOffset.UTC))
                .isRead(entity.isStatus())
                .type(entity.getType())
                .imageUrl(entity.getImageUrl())
                .challengeId(entity.getChallengeId())
                .build();
    }

    public OffsetDateTime createdAt() {
        return this.createdAt;
    }

    public Long id() {
        return this.id;
    }
}
