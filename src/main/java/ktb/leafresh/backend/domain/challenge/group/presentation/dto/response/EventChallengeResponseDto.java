package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;

public record EventChallengeResponseDto(
        Long id,
        String title,
        String description,
        String thumbnailUrl
) {
    public static EventChallengeResponseDto from(GroupChallenge challenge) {
        return new EventChallengeResponseDto(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                challenge.getImageUrl()
        );
    }
}
