package ktb.leafresh.backend.domain.member.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "배지 요약 DTO")
@Builder
public record BadgeSummaryDto(
    @Schema(description = "배지 ID", example = "1") Long id,
    @Schema(description = "배지 이름", example = "첫 걸음") String name,
    @Schema(description = "배지 획득 조건", example = "첫 번째 챌린지 완료") String condition,
    @Schema(description = "배지 이미지 URL", example = "https://example.com/badge.jpg")
        String imageUrl) {}
