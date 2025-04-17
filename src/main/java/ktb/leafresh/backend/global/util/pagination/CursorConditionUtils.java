package ktb.leafresh.backend.global.util.pagination;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.NumberExpression;

import java.time.LocalDateTime;

public class CursorConditionUtils {

    public static boolean useCursor(Long cursorId, String cursorTimestamp) {
        return cursorId != null && cursorTimestamp != null;
    }

    public static LocalDateTime parseTimestamp(String timestamp) {
        return timestamp != null ? LocalDateTime.parse(timestamp) : null;
    }

    /**
     * createdAt + id 복합 커서 조건 생성
     * @param timestampExpr createdAt 또는 다른 타임스탬프 필드 (e.g. gc.createdAt)
     * @param idExpr ID 필드 (e.g. gc.id)
     * @param cursorTimestamp 커서 타임스탬프
     * @param cursorId 커서 ID
     */
    public static BooleanExpression ltCursorWithTimestamp(
            ComparableExpression<LocalDateTime> timestampExpr,
            NumberExpression<Long> idExpr,
            LocalDateTime cursorTimestamp,
            Long cursorId
    ) {
        if (cursorTimestamp == null || cursorId == null) return null;

        return timestampExpr.lt(cursorTimestamp)
                .or(timestampExpr.eq(cursorTimestamp).and(idExpr.lt(cursorId)));
    }
}
