package ktb.leafresh.backend.global.util.pagination;

import lombok.Builder;

import java.util.List;

@Builder
public record CursorPaginationResult<T>(List<T> items, boolean hasNext, CursorInfo cursorInfo) {}
