package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeExampleImage;
import lombok.Builder;

@Builder
public record GroupChallengeExampleImageDto(
        Long id,
        String imageUrl,
        String type,
        String description,
        int sequenceNumber
) {
    public static GroupChallengeExampleImageDto from(GroupChallengeExampleImage image) {
        return GroupChallengeExampleImageDto.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .type(image.getType().name())
                .description(image.getDescription())
                .sequenceNumber(image.getSequenceNumber())
                .build();
    }
}
