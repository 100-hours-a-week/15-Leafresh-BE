package ktb.leafresh.backend.domain.feedback.domain.event;

public record FeedbackCreatedEvent(Long memberId, String content) {}
