package ktb.leafresh.backend.domain.notification.infrastructure.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import ktb.leafresh.backend.domain.notification.domain.entity.Notification;
import ktb.leafresh.backend.domain.notification.domain.entity.QNotification;
import ktb.leafresh.backend.global.util.pagination.CursorConditionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificationReadQueryRepositoryImpl implements NotificationReadQueryRepository {

  private final JPAQueryFactory queryFactory;
  private final QNotification n = QNotification.notification;

  @Override
  public List<Notification> findAllWithCursorAndMemberId(
      LocalDateTime cursorTimestamp, Long cursorId, int size, Long memberId) {
    return queryFactory
        .selectFrom(n)
        .where(
            n.deletedAt.isNull(),
            n.member.id.eq(memberId),
            CursorConditionUtils.ltCursorWithTimestamp(
                n.createdAt, n.id, cursorTimestamp, cursorId))
        .orderBy(n.createdAt.desc(), n.id.desc())
        .limit(size)
        .fetch();
  }
}
