package ktb.leafresh.backend.domain.verification.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인증 개수 응답 DTO")
public record VerificationCountResponseDto(
    @Schema(description = "인증 개수", example = "25") int count) {}
