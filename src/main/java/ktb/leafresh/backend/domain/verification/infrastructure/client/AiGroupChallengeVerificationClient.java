package ktb.leafresh.backend.domain.verification.infrastructure.client;

import ktb.leafresh.backend.domain.verification.infrastructure.dto.request.AiGroupChallengeVerificationRequestDto;

public interface AiGroupChallengeVerificationClient {
    void verifyImage(AiGroupChallengeVerificationRequestDto requestDto);
}
