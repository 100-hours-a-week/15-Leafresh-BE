package ktb.leafresh.backend.global.util.pagination;

import java.util.List;
import java.util.function.Function;

public class CursorPaginationHelper {

    public static <T, D> CursorPaginationResult<D> paginate(
            List<T> entities,
            int size,
            Function<T, D> mapper,
            Function<D, Long> idExtractor
    ) {
        boolean hasNext = entities.size() > size;
        if (hasNext) {
            entities = entities.subList(0, size);
        }

        List<D> dtos = entities.stream()
                .map(mapper)
                .toList();

        Long lastCursorId = dtos.isEmpty() ? null : idExtractor.apply(dtos.get(dtos.size() - 1));

        return CursorPaginationResult.<D>builder()
                .items(dtos)
                .hasNext(hasNext)
                .lastCursorId(lastCursorId)
                .build();
    }
}
