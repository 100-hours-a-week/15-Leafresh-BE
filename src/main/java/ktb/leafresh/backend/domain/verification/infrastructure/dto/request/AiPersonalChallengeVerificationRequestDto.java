package ktb.leafresh.backend.domain.verification.infrastructure.dto.request;

import lombok.Builder;

@Builder
public record AiPersonalChallengeVerificationRequestDto(
        String imageUrl,
        Long memberId,
        Long challengeId,
        String date,
        String challengeName
) {}
