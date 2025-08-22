package ktb.leafresh.backend.domain.verification.infrastructure.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.domain.entity.QGroupChallengeVerification;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.QGroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.QGroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.QGroupChallengeParticipantRecord;
import ktb.leafresh.backend.global.util.pagination.CursorConditionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class GroupChallengeVerificationFeedQueryRepositoryImpl
    implements GroupChallengeVerificationFeedQueryRepository {

  private final JPAQueryFactory queryFactory;

  private final QGroupChallengeVerification v =
      QGroupChallengeVerification.groupChallengeVerification;
  private final QGroupChallengeParticipantRecord pr =
      QGroupChallengeParticipantRecord.groupChallengeParticipantRecord;
  private final QGroupChallenge gc = QGroupChallenge.groupChallenge;
  private final QGroupChallengeCategory cat = QGroupChallengeCategory.groupChallengeCategory;

  @Override
  public List<GroupChallengeVerification> findAllByFilter(
      String category, Long cursorId, String cursorTimestamp, int size) {
    LocalDateTime ts = CursorConditionUtils.parseTimestamp(cursorTimestamp);

    return queryFactory
        .selectFrom(v)
        .join(v.participantRecord, pr)
        .join(pr.groupChallenge, gc)
        .join(gc.category, cat)
        .where(
            v.deletedAt.isNull(),
            eqCategory(category),
            CursorConditionUtils.ltCursorWithTimestamp(v.createdAt, v.id, ts, cursorId))
        .orderBy(v.createdAt.desc(), v.id.desc())
        .limit(size)
        .fetch();
  }

  private BooleanExpression eqCategory(String category) {
    return (category != null && !category.isBlank()) ? cat.name.eq(category) : null;
  }
}
