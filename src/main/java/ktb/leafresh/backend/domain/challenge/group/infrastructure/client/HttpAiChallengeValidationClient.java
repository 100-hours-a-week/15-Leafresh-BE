package ktb.leafresh.backend.domain.challenge.group.infrastructure.client;

import ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.request.AiChallengeValidationRequestDto;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.response.AiChallengeValidationResponseDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Profile("!local")
public class HttpAiChallengeValidationClient implements AiChallengeValidationClient {

    private final WebClient aiServerWebClient;

    public HttpAiChallengeValidationClient(
            @Qualifier("aiServerWebClient") WebClient aiServerWebClient
    ) {
        this.aiServerWebClient = aiServerWebClient;
    }

    @Override
    public AiChallengeValidationResponseDto validateChallenge(AiChallengeValidationRequestDto requestDto) {
        return aiServerWebClient.post()
                .uri("/ai/challenges/group/validation")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(AiChallengeValidationResponseDto.class)
                .block();
    }
}
