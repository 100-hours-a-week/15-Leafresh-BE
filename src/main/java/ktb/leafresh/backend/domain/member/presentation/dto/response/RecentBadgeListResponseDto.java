package ktb.leafresh.backend.domain.member.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "최근 배지 목록 응답 DTO")
@Builder
public record RecentBadgeListResponseDto(
    @Schema(description = "최근 배지 목록") List<BadgeSummaryDto> badges) {}
