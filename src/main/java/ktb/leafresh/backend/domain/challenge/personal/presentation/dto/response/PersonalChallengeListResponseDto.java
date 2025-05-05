package ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record PersonalChallengeListResponseDto(
        List<PersonalChallengeSummaryDto> personalChallenges
) {}
