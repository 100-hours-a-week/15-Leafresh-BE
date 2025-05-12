package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.service.GroupChallengeRemainingDayCalculator;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record GroupChallengeSummaryDto(
        Long id,
        String title,
        String thumbnailUrl,
        int leafReward,
        String startDate,
        String endDate,
        int remainingDay,
        int currentParticipantCount,
        LocalDateTime createdAt
) {
    public static GroupChallengeSummaryDto from(GroupChallenge entity) {
        int remainingDay = GroupChallengeRemainingDayCalculator.calculate(entity.getStartDate().toLocalDate());

        return GroupChallengeSummaryDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .thumbnailUrl(entity.getImageUrl())
                .leafReward(entity.getLeafReward())
                .startDate(entity.getStartDate().toLocalDate().toString())
                .endDate(entity.getEndDate().toLocalDate().toString())
                .currentParticipantCount(entity.getCurrentParticipantCount())
                .createdAt(entity.getCreatedAt())
                .remainingDay(remainingDay)
                .build();
    }

    public static List<GroupChallengeSummaryDto> fromEntities(List<GroupChallenge> entities) {
        return entities.stream().map(GroupChallengeSummaryDto::from).toList();
    }

    public LocalDateTime createdAt() {
        return this.createdAt;
    }
}
