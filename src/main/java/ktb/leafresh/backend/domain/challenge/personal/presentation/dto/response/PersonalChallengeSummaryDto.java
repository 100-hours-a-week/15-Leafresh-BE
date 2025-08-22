package ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import lombok.Builder;

import java.util.List;

@Schema(description = "개인 챌린지 요약 정보")
@Builder
public record PersonalChallengeSummaryDto(
    @Schema(description = "챌린지 ID", example = "1") Long id,
    @Schema(description = "챌린지 제목", example = "매일 물 8잔 마시기") String title,
    @Schema(description = "챌린지 설명", example = "건강한 하루를 위해 물 8잔을 마셔보세요!") String description,
    @Schema(description = "썸네일 이미지 URL", example = "https://leafresh.io/thumbnail.jpg")
        String thumbnailUrl,
    @Schema(description = "리프 보상 포인트", example = "10") int leafReward) {

  public static PersonalChallengeSummaryDto from(PersonalChallenge challenge) {
    return PersonalChallengeSummaryDto.builder()
        .id(challenge.getId())
        .title(challenge.getTitle())
        .description(challenge.getDescription())
        .thumbnailUrl(challenge.getImageUrl())
        .leafReward(challenge.getLeafReward())
        .build();
  }

  public static List<PersonalChallengeSummaryDto> fromEntities(List<PersonalChallenge> challenges) {
    return challenges.stream().map(PersonalChallengeSummaryDto::from).toList();
  }
}
