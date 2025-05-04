package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record GroupChallengeListResponseDto(
        List<GroupChallengeSummaryDto> groupChallenges,
        boolean hasNext,
        Long lastCursorId
) {}
