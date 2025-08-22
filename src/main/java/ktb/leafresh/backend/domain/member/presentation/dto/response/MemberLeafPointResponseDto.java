package ktb.leafresh.backend.domain.member.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "회원 리프 포인트 응답 DTO")
@Getter
@AllArgsConstructor
public class MemberLeafPointResponseDto {

  @Schema(description = "현재 리프 포인트", example = "150")
  private Integer currentLeafPoints;
}
