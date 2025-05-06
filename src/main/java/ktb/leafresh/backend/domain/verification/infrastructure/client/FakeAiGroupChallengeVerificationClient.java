package ktb.leafresh.backend.domain.verification.infrastructure.client;

import ktb.leafresh.backend.domain.verification.infrastructure.dto.request.AiGroupChallengeVerificationRequestDto;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class FakeAiGroupChallengeVerificationClient implements AiGroupChallengeVerificationClient {

    @Override
    public void verifyImage(AiGroupChallengeVerificationRequestDto requestDto) {
        System.out.println("AI 서버 호출 시뮬레이션 (local) [GROUP]: " + requestDto);
    }
}
