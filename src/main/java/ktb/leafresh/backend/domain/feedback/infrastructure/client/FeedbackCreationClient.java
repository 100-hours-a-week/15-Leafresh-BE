package ktb.leafresh.backend.domain.feedback.infrastructure.client;

import ktb.leafresh.backend.domain.feedback.infrastructure.dto.request.AiFeedbackCreationRequestDto;

public interface FeedbackCreationClient {
  void requestWeeklyFeedback(AiFeedbackCreationRequestDto requestDto);
}
