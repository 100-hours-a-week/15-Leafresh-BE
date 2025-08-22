package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "단체 챌린지 참여 상태별 개수")
public record GroupChallengeParticipationCountSummaryDto(
    @Schema(description = "시작 전", example = "10") int notStarted,
    @Schema(description = "진행 중", example = "25") int ongoing,
    @Schema(description = "완료", example = "15") int completed) {}
