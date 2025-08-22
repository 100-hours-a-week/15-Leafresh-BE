package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "단체 챌린지 생성 응답")
public record GroupChallengeCreateResponseDto(@Schema(description = "생성된 챌린지 ID") Long id) {}
