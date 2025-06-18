package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record GroupChallengeParticipationSummaryDto(
        Long id,
        String title,
        String thumbnailUrl,
        String startDate,
        String endDate,
        AchievementDto achievement,
        List<AchievementRecordDto> achievementRecords,
        @JsonIgnore
        OffsetDateTime createdAt
) {
    @Builder
    public record AchievementDto(Long success, Long total) {}

    @Builder
    public record AchievementRecordDto(int day, String status) {}

    public static GroupChallengeParticipationSummaryDto of(
            Long id,
            String title,
            String thumbnailUrl,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Long success,
            Long total,
            List<AchievementRecordDto> achievementRecords,
            OffsetDateTime createdAt
    ) {
        return GroupChallengeParticipationSummaryDto.builder()
                .id(id)
                .title(title)
                .thumbnailUrl(thumbnailUrl)
                .startDate(startDate.toString())
                .endDate(endDate.toString())
                .achievement(new AchievementDto(success, total))
                .achievementRecords(achievementRecords)
                .createdAt(createdAt)
                .build();
    }
}
