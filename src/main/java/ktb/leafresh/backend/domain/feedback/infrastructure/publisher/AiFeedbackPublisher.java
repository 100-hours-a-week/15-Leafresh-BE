package ktb.leafresh.backend.domain.feedback.infrastructure.publisher;

import ktb.leafresh.backend.domain.feedback.infrastructure.dto.request.AiFeedbackCreationRequestDto;

public interface AiFeedbackPublisher {
    void publishAsyncWithRetry(AiFeedbackCreationRequestDto dto);
}
