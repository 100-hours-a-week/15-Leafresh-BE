package ktb.leafresh.backend.global.util.pagination;

import lombok.Builder;

@Builder
public record CursorInfo(
    Long lastCursorId, String cursorTimestamp // ISO 8601 형식 (e.g., "2025-05-01T10:00:00")
    ) {}
