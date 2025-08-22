package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.QGroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeParticipationSummaryDto;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.domain.entity.QGroupChallengeVerification;
import ktb.leafresh.backend.global.util.pagination.CursorConditionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class GroupChallengeVerificationQueryRepositoryImpl
    implements GroupChallengeVerificationQueryRepository {

  private final JPAQueryFactory queryFactory;
  private final QGroupChallengeVerification gv =
      QGroupChallengeVerification.groupChallengeVerification;
  private final QGroupChallengeVerification verification =
      QGroupChallengeVerification.groupChallengeVerification;
  private final QGroupChallengeParticipantRecord record =
      QGroupChallengeParticipantRecord.groupChallengeParticipantRecord;

  @Override
  public List<GroupChallengeVerification> findByChallengeId(
      Long challengeId, Long cursorId, String cursorTimestamp, int size) {
    LocalDateTime ts = CursorConditionUtils.parseTimestamp(cursorTimestamp);

    return queryFactory
        .selectFrom(gv)
        .where(
            gv.participantRecord.groupChallenge.id.eq(challengeId),
            gv.deletedAt.isNull(),
            CursorConditionUtils.ltCursorWithTimestamp(gv.createdAt, gv.id, ts, cursorId))
        .orderBy(gv.createdAt.desc(), gv.id.desc())
        .limit(size)
        .fetch();
  }

  private BooleanExpression ltCursor(LocalDateTime ts, Long id) {
    if (ts == null || id == null) return null;
    return gv.createdAt.lt(ts).or(gv.createdAt.eq(ts).and(gv.id.lt(id)));
  }

  @Override
  public List<GroupChallengeVerification> findByParticipantRecordId(Long participantRecordId) {
    return queryFactory
        .selectFrom(gv)
        .where(gv.participantRecord.id.eq(participantRecordId), gv.deletedAt.isNull())
        .orderBy(gv.createdAt.desc())
        .fetch();
  }

  @Override
  public Optional<GroupChallengeVerification> findByChallengeIdAndId(
      Long challengeId, Long verificationId) {
    QGroupChallengeVerification v = QGroupChallengeVerification.groupChallengeVerification;

    return Optional.ofNullable(
        queryFactory
            .selectFrom(v)
            .join(v.participantRecord)
            .fetchJoin()
            .join(v.participantRecord.groupChallenge)
            .fetchJoin()
            .join(v.participantRecord.member)
            .fetchJoin()
            .where(
                v.id.eq(verificationId),
                v.participantRecord.groupChallenge.id.eq(challengeId),
                v.deletedAt.isNull())
            .fetchOne());
  }

  @Override
  public Map<Long, List<GroupChallengeParticipationSummaryDto.AchievementRecordDto>>
      findVerificationsGroupedByChallenge(List<Long> challengeIds, Long memberId) {

    List<Tuple> results =
        queryFactory
            .select(record.groupChallenge.id, verification.status, verification.createdAt)
            .from(verification)
            .join(verification.participantRecord, record)
            .where(
                record.groupChallenge.id.in(challengeIds),
                record.member.id.eq(memberId),
                verification.deletedAt.isNull())
            .orderBy(verification.createdAt.asc())
            .fetch();

    Map<Long, List<GroupChallengeParticipationSummaryDto.AchievementRecordDto>> map =
        new HashMap<>();
    Map<Long, Integer> challengeIdToDayCounter = new HashMap<>();

    for (Tuple tuple : results) {
      Long challengeId = tuple.get(record.groupChallenge.id);
      String status = tuple.get(verification.status).name();
      int day = challengeIdToDayCounter.merge(challengeId, 1, Integer::sum);

      map.computeIfAbsent(challengeId, k -> new ArrayList<>())
          .add(new GroupChallengeParticipationSummaryDto.AchievementRecordDto(day, status));
    }

    return map;
  }
}
