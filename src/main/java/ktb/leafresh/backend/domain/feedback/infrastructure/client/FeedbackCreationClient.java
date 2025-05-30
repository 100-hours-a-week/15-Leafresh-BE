package ktb.leafresh.backend.domain.feedback.infrastructure.client;

import ktb.leafresh.backend.domain.feedback.infrastructure.dto.request.FeedbackCreationRequestDto;

public interface FeedbackCreationClient {
    void requestWeeklyFeedback(FeedbackCreationRequestDto requestDto);
}
