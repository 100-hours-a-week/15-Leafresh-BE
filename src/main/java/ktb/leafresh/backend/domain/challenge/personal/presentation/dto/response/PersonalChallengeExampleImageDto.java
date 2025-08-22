package ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallengeExampleImage;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import lombok.Builder;

@Schema(description = "개인 챌린지 예시 이미지 정보")
@Builder
public record PersonalChallengeExampleImageDto(
    @Schema(description = "예시 이미지 ID", example = "1") Long id,
    @Schema(description = "이미지 URL", example = "https://leafresh.io/example.jpg") String imageUrl,
    @Schema(description = "이미지 설명", example = "올바른 인증 예시") String description,
    @Schema(description = "순서 번호", example = "1") int sequenceNumber,
    @Schema(description = "이미지 타입", example = "SUCCESS/FAILURE") String type) {

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
