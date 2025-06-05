package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.domain.entity.QGroupChallengeVerification;
import ktb.leafresh.backend.global.util.pagination.CursorConditionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GroupChallengeVerificationQueryRepositoryImpl implements GroupChallengeVerificationQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QGroupChallengeVerification gv = QGroupChallengeVerification.groupChallengeVerification;

    @Override
    public List<GroupChallengeVerification> findByChallengeId(Long challengeId, Long cursorId, String cursorTimestamp, int size) {
        LocalDateTime ts = CursorConditionUtils.parseTimestamp(cursorTimestamp);

        return queryFactory.selectFrom(gv)
                .where(
                        gv.participantRecord.groupChallenge.id.eq(challengeId),
                        gv.deletedAt.isNull(),
                        CursorConditionUtils.ltCursorWithTimestamp(gv.createdAt, gv.id, ts, cursorId)
                )
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
        return queryFactory.selectFrom(gv)
                .where(gv.participantRecord.id.eq(participantRecordId), gv.deletedAt.isNull())
                .orderBy(gv.createdAt.desc())
                .fetch();
    }

    @Override
    public Optional<GroupChallengeVerification> findByChallengeIdAndId(Long challengeId, Long verificationId) {
        QGroupChallengeVerification v = QGroupChallengeVerification.groupChallengeVerification;

        return Optional.ofNullable(queryFactory
                .selectFrom(v)
                .join(v.participantRecord).fetchJoin()
                .join(v.participantRecord.groupChallenge).fetchJoin()
                .join(v.participantRecord.member).fetchJoin()
                .where(
                        v.id.eq(verificationId),
                        v.participantRecord.groupChallenge.id.eq(challengeId),
                        v.deletedAt.isNull()
                )
                .fetchOne());
    }
}
