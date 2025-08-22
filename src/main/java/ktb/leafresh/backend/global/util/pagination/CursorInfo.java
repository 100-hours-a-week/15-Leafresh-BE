package ktb.leafresh.backend.global.util.pagination;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "커서 페이지네이션 정보")
@Builder
public record CursorInfo(
    @Schema(description = "마지막 커서 ID", example = "100") Long lastCursorId,
    @Schema(description = "커서 타임스탬프 (ISO 8601 형식)", example = "2025-05-01T10:00:00")
        String cursorTimestamp // ISO 8601 형식 (e.g., "2025-05-01T10:00:00")
    ) {}
