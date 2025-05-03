package ktb.leafresh.backend.domain.challenge.group.application.validator;

import ktb.leafresh.backend.domain.challenge.group.infrastructure.client.AiChallengeValidationClient;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.AiChallengeValidationRequestDto;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.AiChallengeValidationResponseDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeCreateRequestDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiChallengePolicyValidator {

    private final AiChallengeValidationClient aiChallengeValidationClient;

    public void validate(Long memberId, GroupChallengeCreateRequestDto dto) {
        AiChallengeValidationRequestDto aiRequest = new AiChallengeValidationRequestDto(
                memberId,
                dto.title(),
                dto.startDate().toString(),
                dto.endDate().toString()
        );

        AiChallengeValidationResponseDto aiResponse = aiChallengeValidationClient.validateChallenge(aiRequest);

        if (!aiResponse.result()) {
            throw new CustomException(ErrorCode.CHALLENGE_CREATION_REJECTED_BY_AI);
        }
    }
}
