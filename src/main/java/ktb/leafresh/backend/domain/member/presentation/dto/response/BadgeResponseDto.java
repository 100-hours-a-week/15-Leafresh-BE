package ktb.leafresh.backend.domain.member.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "배지 정보 응답 DTO")
@Getter
@Builder
public class BadgeResponseDto {

  @Schema(description = "배지 ID", example = "1")
  private Long id;

  @Schema(description = "배지 이름", example = "첫 걸음")
  private String name;

  @Schema(description = "배지 획득 조건", example = "첫 번째 챌린지 완료")
  private String condition;

  @Schema(description = "배지 이미지 URL", example = "https://example.com/badge.jpg")
  private String imageUrl;

  @Schema(description = "배지 잠금 여부", example = "false")
  @JsonProperty("isLocked")
  private boolean isLocked;

  public static BadgeResponseDto of(Badge badge, boolean isLocked, String lockImageUrl) {
    return BadgeResponseDto.builder()
        .id(badge.getId())
        .name(badge.getName())
        .condition(badge.getCondition())
        .imageUrl(isLocked ? lockImageUrl : badge.getImageUrl())
        .isLocked(isLocked)
        .build();
  }
}
