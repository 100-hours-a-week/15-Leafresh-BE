package ktb.leafresh.backend.domain.verification.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Schema(description = "댓글 목록 응답 DTO")
@Getter
@AllArgsConstructor
public class CommentListResponseDto {
  @Schema(description = "댓글 목록")
  private List<CommentSummaryResponseDto> comments;
}
