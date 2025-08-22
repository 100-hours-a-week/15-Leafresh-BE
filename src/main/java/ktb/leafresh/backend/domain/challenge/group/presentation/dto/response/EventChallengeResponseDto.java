package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;

@Schema(description = "이벤트 챌린지 정보")
public record EventChallengeResponseDto(
    @Schema(description = "챌린지 ID", example = "1") Long id,
    @Schema(description = "챌린지 제목", example = "특별 이벤트 챌린지") String title,
    @Schema(description = "챌린지 설명", example = "특별한 이벤트 챌린지에 참여해보세요!") String description,
    @Schema(description = "썸네일 이미지 URL", example = "https://leafresh.io/event-thumbnail.jpg")
        String thumbnailUrl) {

  public static EventChallengeResponseDto from(GroupChallenge challenge) {
    return new EventChallengeResponseDto(
        challenge.getId(),
        challenge.getTitle(),
        challenge.getDescription(),
        challenge.getImageUrl());
  }
}
