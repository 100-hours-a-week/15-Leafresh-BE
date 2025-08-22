package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.global.util.pagination.CursorInfo;
import lombok.Builder;

import java.util.List;

@Schema(description = "단체 챌린지 참여 목록 응답")
@Builder
public record GroupChallengeParticipationListResponseDto(
    @Schema(description = "참여 중인 챌린지 목록") List<GroupChallengeParticipationSummaryDto> challenges,
    @Schema(description = "다음 페이지 존재 여부", example = "true") boolean hasNext,
    @Schema(description = "커서 페이지네이션 정보") CursorInfo cursorInfo) {}
