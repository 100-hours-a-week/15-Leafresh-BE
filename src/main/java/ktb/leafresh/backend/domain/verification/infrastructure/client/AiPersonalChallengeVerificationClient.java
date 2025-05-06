package ktb.leafresh.backend.domain.verification.infrastructure.client;

import ktb.leafresh.backend.domain.verification.infrastructure.dto.request.AiPersonalChallengeVerificationRequestDto;

public interface AiPersonalChallengeVerificationClient {
    void verifyImage(AiPersonalChallengeVerificationRequestDto requestDto);
}
