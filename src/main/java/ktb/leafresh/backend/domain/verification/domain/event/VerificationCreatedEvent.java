package ktb.leafresh.backend.domain.verification.domain.event;

import ktb.leafresh.backend.domain.verification.infrastructure.dto.request.AiVerificationRequestDto;

public record VerificationCreatedEvent(AiVerificationRequestDto requestDto) {}
