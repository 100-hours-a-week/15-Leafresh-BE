package ktb.leafresh.backend.domain.verification.infrastructure.client;

import ktb.leafresh.backend.domain.verification.infrastructure.dto.request.AiPersonalChallengeVerificationRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Profile("!local")
@RequiredArgsConstructor
public class HttpAiPersonalChallengeVerificationClient implements AiPersonalChallengeVerificationClient {

    private final WebClient aiServerWebClient;

    @Override
    public void verifyImage(AiPersonalChallengeVerificationRequestDto requestDto) {
        aiServerWebClient.post()
                .uri("/ai/image/verification")
                .bodyValue(requestDto)
                .retrieve()
                .toBodilessEntity()
                .block(); // 동기 호출
    }
}
