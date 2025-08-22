package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Schema(description = "생성한 단체 챌린지 요약 정보")
@Builder
public record CreatedGroupChallengeSummaryResponseDto(
    @Schema(description = "챌린지 ID", example = "1") Long id,
    @Schema(description = "챌린지 이름", example = "제로웨이스트 챌린지") String name,
    @Schema(description = "챌린지 설명", example = "환경을 위한 제로웨이스트 챌린지에 참여해보세요!") String description,
    @Schema(description = "시작일", example = "2024-01-01") String startDate,
    @Schema(description = "종료일", example = "2024-01-31") String endDate,
    @Schema(description = "썸네일 이미지 URL", example = "https://leafresh.io/thumbnail.jpg")
        String imageUrl,
    @Schema(description = "현재 참여 인원", example = "25") int currentParticipantCount,
    @Schema(description = "카테고리", example = "ZERO_WASTE") String category,
    @JsonIgnore @Schema(hidden = true) OffsetDateTime createdAt) {

  public static CreatedGroupChallengeSummaryResponseDto from(GroupChallenge entity) {
    return CreatedGroupChallengeSummaryResponseDto.builder()
        .id(entity.getId())
        .name(entity.getTitle())
        .description(entity.getDescription())
        .startDate(entity.getStartDate().toLocalDate().toString())
        .endDate(entity.getEndDate().toLocalDate().toString())
        .imageUrl(entity.getImageUrl())
        .currentParticipantCount(entity.getCurrentParticipantCount())
        .createdAt(entity.getCreatedAt().atOffset(ZoneOffset.UTC))
        .category(entity.getCategory().getName())
        .build();
  }

  public static List<CreatedGroupChallengeSummaryResponseDto> fromEntities(
      List<GroupChallenge> entities) {
    return entities.stream().map(CreatedGroupChallengeSummaryResponseDto::from).toList();
  }

  public OffsetDateTime createdAt() {
    return createdAt;
  }
}
