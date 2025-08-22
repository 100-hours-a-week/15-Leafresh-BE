package ktb.leafresh.backend.global.util.pagination;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

public class CursorPaginationHelper {

  public static <T, D> CursorPaginationResult<D> paginateWithTimestamp(
      List<T> entities,
      int size,
      Function<T, D> mapper,
      Function<D, Long> idExtractor,
      Function<D, LocalDateTime> timestampExtractor) {
    boolean hasNext = entities.size() > size;
    if (hasNext) entities = entities.subList(0, size);

    List<D> dtos = entities.stream().map(mapper).toList();

    CursorInfo cursorInfo =
        dtos.isEmpty()
            ? new CursorInfo(null, null)
            : new CursorInfo(
                idExtractor.apply(dtos.getLast()),
                timestampExtractor.apply(dtos.getLast()).toString());

    return new CursorPaginationResult<>(dtos, hasNext, cursorInfo);
  }
}
