package ktb.leafresh.backend.domain.notification.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.notification.domain.entity.Notification;
import ktb.leafresh.backend.domain.notification.domain.entity.enums.NotificationType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Schema(description = "알림 요약 정보")
@Builder
public record NotificationSummaryResponse(
    @Schema(description = "알림 ID", example = "1") Long id,
    @Schema(description = "알림 제목", example = "새로운 챌린지가 시작되었습니다!") String title,
    @Schema(description = "알림 내용", example = "7일 간의 환경 보호 챌린지에 참여해보세요.") String content,
    @Schema(description = "알림 생성 시간 (UTC)", example = "2025-01-15T10:30:00Z")
        OffsetDateTime createdAt,
    @Schema(description = "읽음 상태", example = "false") boolean isRead,
    @Schema(description = "알림 유형") NotificationType type,
    @Schema(description = "알림 이미지 URL", example = "https://example.com/image.jpg") String imageUrl,
    @Schema(description = "연관된 챌린지 ID", example = "42") Long challengeId) {
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
