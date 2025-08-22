package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

public record GroupChallengeParticipationCountSummaryDto(
    int notStarted, int ongoing, int completed) {}
