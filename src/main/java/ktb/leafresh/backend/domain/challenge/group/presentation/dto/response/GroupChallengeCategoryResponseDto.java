package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "단체 챌린지 카테고리 정보")
@Getter
@Builder
@AllArgsConstructor
public class GroupChallengeCategoryResponseDto {
  @Schema(description = "카테고리 코드", example = "ZERO_WASTE")
  private String category;

  @Schema(description = "카테고리 이름", example = "제로웨이스트")
  private String label;

  @Schema(description = "카테고리 이미지 URL", example = "https://leafresh.io/category.jpg")
  private String imageUrl;
}
