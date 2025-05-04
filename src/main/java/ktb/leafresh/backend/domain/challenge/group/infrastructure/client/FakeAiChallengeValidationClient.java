package ktb.leafresh.backend.domain.challenge.group.infrastructure.client;

import ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.AiChallengeValidationRequestDto;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.AiChallengeValidationResponseDto;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class FakeAiChallengeValidationClient implements AiChallengeValidationClient {

    @Override
    public AiChallengeValidationResponseDto validateChallenge(AiChallengeValidationRequestDto requestDto) {
        // Always allow challenge creation during local development
        return new AiChallengeValidationResponseDto(true);
    }
}
