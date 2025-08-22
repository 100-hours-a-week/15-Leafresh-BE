package ktb.leafresh.backend.domain.verification.infrastructure.publisher;

import ktb.leafresh.backend.domain.verification.infrastructure.dto.request.AiVerificationRequestDto;

public interface AiVerificationPublisher {
  void publishAsyncWithRetry(AiVerificationRequestDto dto);
}
