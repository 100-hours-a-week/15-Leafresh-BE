package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record GroupChallengeParticipationListResponseDto(
        List<GroupChallengeParticipationSummaryDto> challenges,
        boolean hasNext,
        Long lastCursorId
) {}
