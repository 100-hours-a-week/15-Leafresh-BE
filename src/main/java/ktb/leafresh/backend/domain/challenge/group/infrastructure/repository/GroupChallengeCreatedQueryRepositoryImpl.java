package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.QGroupChallenge;
import ktb.leafresh.backend.global.util.pagination.CursorConditionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class GroupChallengeCreatedQueryRepositoryImpl
    implements GroupChallengeCreatedQueryRepository {

  private final JPAQueryFactory queryFactory;
  private final QGroupChallenge gc = QGroupChallenge.groupChallenge;

  @Override
  public List<GroupChallenge> findCreatedByMember(
      Long memberId, Long cursorId, String cursorTimestamp, int size) {
    LocalDateTime ts = CursorConditionUtils.parseTimestamp(cursorTimestamp);

    return queryFactory
        .selectFrom(gc)
        .where(
            gc.deletedAt.isNull(),
            gc.member.id.eq(memberId),
            CursorConditionUtils.ltCursorWithTimestamp(gc.createdAt, gc.id, ts, cursorId))
        .orderBy(gc.createdAt.desc(), gc.id.desc())
        .limit(size)
        .fetch();
  }

  private BooleanExpression ltCursor(LocalDateTime ts, Long id) {
    if (ts == null || id == null) return null;
    return gc.createdAt.lt(ts).or(gc.createdAt.eq(ts).and(gc.id.lt(id)));
  }
}
