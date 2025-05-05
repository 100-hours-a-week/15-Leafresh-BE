package ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import lombok.Builder;

import java.util.List;

@Builder
public record PersonalChallengeSummaryDto(
        Long id,
        String title,
        String description,
        String imageUrl,
        int leafReward
) {
    public static PersonalChallengeSummaryDto from(PersonalChallenge challenge) {
        return PersonalChallengeSummaryDto.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .imageUrl(challenge.getImageUrl())
                .leafReward(challenge.getLeafReward())
                .build();
    }

    public static List<PersonalChallengeSummaryDto> fromEntities(List<PersonalChallenge> challenges) {
        return challenges.stream().map(PersonalChallengeSummaryDto::from).toList();
    }
}
