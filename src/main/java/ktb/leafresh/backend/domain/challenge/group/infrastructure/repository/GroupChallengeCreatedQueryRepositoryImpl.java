package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.QGroupChallenge;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GroupChallengeCreatedQueryRepositoryImpl implements GroupChallengeCreatedQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QGroupChallenge gc = QGroupChallenge.groupChallenge;

    @Override
    public List<GroupChallenge> findCreatedByMember(Long memberId, Long cursorId, int size) {
        return queryFactory.selectFrom(gc)
                .where(
                        gc.deletedAt.isNull(),
                        gc.member.id.eq(memberId),
                        ltCursorId(cursorId)
                )
                .orderBy(gc.id.desc())
                .limit(size + 1)
                .fetch();
    }

    private BooleanExpression ltCursorId(Long cursorId) {
        return cursorId != null ? gc.id.lt(cursorId) : null;
    }
}
