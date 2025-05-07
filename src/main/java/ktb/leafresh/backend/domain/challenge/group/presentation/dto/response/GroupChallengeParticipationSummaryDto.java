package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import lombok.Builder;

@Builder
public record GroupChallengeParticipationSummaryDto(
        Long id,
        String title,
        String thumbnailUrl,
        String startDate,
        String endDate,
        AchievementDto achievement
) {
    @Builder
    public record AchievementDto(Long success, Long total) {}

    public static GroupChallengeParticipationSummaryDto of(
            Long id, String title, String thumbnailUrl, String startDate,
            String endDate, Long success, Long total
    ) {
        return GroupChallengeParticipationSummaryDto.builder()
                .id(id)
                .title(title)
                .thumbnailUrl(thumbnailUrl)
                .startDate(startDate)
                .endDate(endDate)
                .achievement(new AchievementDto(success, total))
                .build();
    }
}
