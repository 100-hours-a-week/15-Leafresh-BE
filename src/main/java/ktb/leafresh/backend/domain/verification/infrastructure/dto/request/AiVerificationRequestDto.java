package ktb.leafresh.backend.domain.verification.infrastructure.dto.request;

import ktb.leafresh.backend.global.common.entity.enums.ChallengeType;
import lombok.Builder;

@Builder
public record AiVerificationRequestDto(
    Long verificationId,
    ChallengeType type, // PERSONAL or GROUP
    String imageUrl,
    Long memberId,
    Long challengeId,
    String date,
    String challengeName,
    String challengeInfo) {}
