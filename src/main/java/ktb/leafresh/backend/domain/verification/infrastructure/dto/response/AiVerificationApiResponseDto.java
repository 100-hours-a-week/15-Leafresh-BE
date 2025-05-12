package ktb.leafresh.backend.domain.verification.infrastructure.dto.response;

public record AiVerificationApiResponseDto(
        int status,
        String message,
        AiVerificationResponseDto data
) {}
