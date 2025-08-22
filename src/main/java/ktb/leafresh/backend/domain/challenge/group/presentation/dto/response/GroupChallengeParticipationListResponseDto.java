package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import ktb.leafresh.backend.global.util.pagination.CursorInfo;
import lombok.Builder;

import java.util.List;

@Builder
public record GroupChallengeParticipationListResponseDto(
    List<GroupChallengeParticipationSummaryDto> challenges,
    boolean hasNext,
    CursorInfo cursorInfo) {}
