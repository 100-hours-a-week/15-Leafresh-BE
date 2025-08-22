package ktb.leafresh.backend.domain.member.presentation.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record RecentBadgeListResponseDto(List<BadgeSummaryDto> badges) {}
