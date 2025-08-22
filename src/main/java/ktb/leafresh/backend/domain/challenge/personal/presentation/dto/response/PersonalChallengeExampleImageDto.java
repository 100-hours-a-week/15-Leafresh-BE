package ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallengeExampleImage;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import lombok.Builder;

@Builder
public record PersonalChallengeExampleImageDto(
    Long id, String imageUrl, String description, int sequenceNumber, String type) {
  public static PersonalChallengeExampleImageDto from(PersonalChallengeExampleImage image) {
    return PersonalChallengeExampleImageDto.builder()
        .id(image.getId())
        .imageUrl(image.getImageUrl())
        .description(image.getDescription())
        .sequenceNumber(image.getSequenceNumber())
        .type(image.getType().name())
        .build();
  }
}
