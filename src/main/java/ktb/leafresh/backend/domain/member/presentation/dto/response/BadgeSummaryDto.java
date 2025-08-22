package ktb.leafresh.backend.domain.member.presentation.dto.response;

import lombok.Builder;

@Builder
public record BadgeSummaryDto(Long id, String name, String condition, String imageUrl) {}
