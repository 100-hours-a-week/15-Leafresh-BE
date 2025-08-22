package ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.response;

public record AiChallengeValidationApiResponseDto(
    int status, String message, AiChallengeValidationResponseDto data) {}
