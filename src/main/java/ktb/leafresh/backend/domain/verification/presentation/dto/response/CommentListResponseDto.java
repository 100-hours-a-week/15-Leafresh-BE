package ktb.leafresh.backend.domain.verification.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CommentListResponseDto {
    private List<CommentSummaryResponseDto> comments;
}
