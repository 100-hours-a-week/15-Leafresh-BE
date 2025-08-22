package ktb.leafresh.backend.domain.verification.infrastructure.dto.response;

public record AiVerificationResponseDto(boolean result // true: 검열 통과, false: 부적절 이미지
    ) {}
