package ktb.leafresh.backend.domain.member.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "총 리프 포인트 응답 DTO")
public record TotalLeafPointResponseDto(
    @Schema(description = "총 리프 포인트 개수", example = "1500") int count) {}
