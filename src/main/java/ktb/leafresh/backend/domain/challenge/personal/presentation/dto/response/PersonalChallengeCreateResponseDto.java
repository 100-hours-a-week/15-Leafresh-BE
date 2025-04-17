package ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "개인 챌린지 템플릿 생성 응답")
public record PersonalChallengeCreateResponseDto(
        @Schema(description = "생성된 템플릿 ID") Long id
) {}
