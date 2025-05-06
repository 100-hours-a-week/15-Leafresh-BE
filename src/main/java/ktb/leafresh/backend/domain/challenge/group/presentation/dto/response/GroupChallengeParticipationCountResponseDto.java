package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import lombok.Builder;

@Builder
public record GroupChallengeParticipationCountResponseDto(
        GroupChallengeParticipationCountSummaryDto count
) {
    public static GroupChallengeParticipationCountResponseDto from(GroupChallengeParticipationCountSummaryDto summary) {
        return new GroupChallengeParticipationCountResponseDto(summary);
    }
}
