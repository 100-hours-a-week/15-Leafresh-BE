package ktb.leafresh.backend.domain.feedback.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FeedbackResponseDto {
  private final String content; // null 가능
}
