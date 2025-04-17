package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GroupChallengeParticipationSummaryDto(
        Long id,
        String title,
        String thumbnailUrl,
        String startDate,
        String endDate,
        AchievementDto achievement,
        LocalDateTime createdAt
) {
    @Builder
    public record AchievementDto(Long success, Long total) {}

    public static GroupChallengeParticipationSummaryDto of(
            Long id,
            String title,
            String thumbnailUrl,
            String startDate,
            String endDate,
            Long success,
            Long total,
            LocalDateTime createdAt
    ) {
        return GroupChallengeParticipationSummaryDto.builder()
                .id(id)
                .title(title)
                .thumbnailUrl(thumbnailUrl)
                .startDate(startDate)
                .endDate(endDate)
                .achievement(new AchievementDto(success, total))
                .createdAt(createdAt)
                .build();
    }
}
