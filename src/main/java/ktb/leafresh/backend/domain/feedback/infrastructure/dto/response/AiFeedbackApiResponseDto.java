package ktb.leafresh.backend.domain.feedback.infrastructure.dto.response;

public record AiFeedbackApiResponseDto(int status, String message, AiFeedbackResponseDto data) {}
