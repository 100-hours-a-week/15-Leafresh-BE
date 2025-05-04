package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.QGroupChallenge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class GroupChallengeQueryRepositoryImpl implements GroupChallengeQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QGroupChallenge gc = QGroupChallenge.groupChallenge;

    @Override
    public List<GroupChallenge> findByCategoryWithSearch(Long categoryId, String input, Long cursorId, int size) {
        return queryFactory.selectFrom(gc)
                .where(
                        gc.category.id.eq(categoryId),
                        gc.deletedAt.isNull(),
                        gc.endDate.goe(LocalDateTime.now()),
                        likeInput(input),
                        ltCursorId(cursorId)
                )
                .orderBy(gc.id.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression likeInput(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        return gc.title.containsIgnoreCase(input)
                .or(gc.description.containsIgnoreCase(input));
    }

    private BooleanExpression ltCursorId(Long cursorId) {
        return cursorId != null ? gc.id.lt(cursorId) : null;
    }
}
