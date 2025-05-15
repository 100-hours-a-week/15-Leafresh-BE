package ktb.leafresh.backend.domain.verification.infrastructure.client;

import ktb.leafresh.backend.domain.verification.infrastructure.dto.request.AiVerificationRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
public class FakeAiVerificationClient implements AiVerificationClient {

    @Override
    public void verifyImage(AiVerificationRequestDto requestDto) {
        log.info("[FAKE AI 클라이언트] 로컬 환경에서 AI 이미지 검증 요청 무시됨. 요청 DTO: {}", requestDto);
    }
}
