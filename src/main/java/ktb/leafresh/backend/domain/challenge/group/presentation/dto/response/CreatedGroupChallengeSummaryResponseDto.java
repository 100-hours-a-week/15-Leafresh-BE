package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CreatedGroupChallengeSummaryResponseDto(
        Long id,
        String name,
        String description,
        String startDate,
        String endDate,
        String imageUrl,
        int currentParticipantCount,
        String category,
        @JsonIgnore
        LocalDateTime createdAt
) {
    public static CreatedGroupChallengeSummaryResponseDto from(GroupChallenge entity) {
        return CreatedGroupChallengeSummaryResponseDto.builder()
                .id(entity.getId())
                .name(entity.getTitle())
                .description(entity.getDescription())
                .startDate(entity.getStartDate().toLocalDate().toString())
                .endDate(entity.getEndDate().toLocalDate().toString())
                .imageUrl(entity.getImageUrl())
                .currentParticipantCount(entity.getCurrentParticipantCount())
                .createdAt(entity.getCreatedAt())
                .category(entity.getCategory().getName())
                .build();
    }

    public static List<CreatedGroupChallengeSummaryResponseDto> fromEntities(List<GroupChallenge> entities) {
        return entities.stream().map(CreatedGroupChallengeSummaryResponseDto::from).toList();
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }
}
