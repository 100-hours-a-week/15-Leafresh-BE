package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.QGroupChallenge;
import ktb.leafresh.backend.global.util.pagination.CursorConditionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class GroupChallengeSearchQueryRepositoryImpl implements GroupChallengeSearchQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QGroupChallenge gc = QGroupChallenge.groupChallenge;

    @Override
    public List<GroupChallenge> findByFilter(String input, String category, Long cursorId, String cursorTimestamp, int size) {
        LocalDateTime ts = CursorConditionUtils.parseTimestamp(cursorTimestamp);

        return queryFactory.selectFrom(gc)
                .where(
                        gc.deletedAt.isNull(),
                        gc.endDate.goe(LocalDateTime.now()),
                        likeInput(input),
                        eqCategory(category),
                        CursorConditionUtils.ltCursorWithTimestamp(gc.createdAt, gc.id, ts, cursorId)
                )
                .orderBy(gc.createdAt.desc(), gc.id.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression likeInput(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        return gc.title.containsIgnoreCase(input)
                .or(gc.description.containsIgnoreCase(input));
    }

    private BooleanExpression eqCategory(String category) {
        return category != null ? gc.category.name.eq(category) : null;
    }

    private BooleanExpression ltCursor(LocalDateTime ts, Long id) {
        if (ts == null || id == null) return null;
        return gc.createdAt.lt(ts)
                .or(gc.createdAt.eq(ts).and(gc.id.lt(id)));
    }
}
