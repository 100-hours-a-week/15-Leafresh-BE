package ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 챌린지 검증 응답 DTO")
public record AiChallengeValidationResponseDto(
    @Schema(description = "검증 결과 (true: 생성 가능, false: 유사 챌린지 존재)", example = "true")
        boolean result // true = 생성 가능, false = 유사 챌린지 존재
    ) {}
