package ktb.leafresh.backend.domain.verification.infrastructure.client;

import ktb.leafresh.backend.domain.verification.infrastructure.dto.request.AiVerificationRequestDto;

public interface AiVerificationClient {
    void verifyImage(AiVerificationRequestDto requestDto);
}
