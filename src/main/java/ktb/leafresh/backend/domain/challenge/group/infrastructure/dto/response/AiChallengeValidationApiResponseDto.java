package ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 챌린지 검증 API 응답 래퍼")
public record AiChallengeValidationApiResponseDto(
    @Schema(description = "응답 상태 코드", example = "200") int status,
    @Schema(description = "응답 메시지", example = "Success") String message,
    @Schema(description = "검증 결과 데이터") AiChallengeValidationResponseDto data) {}
