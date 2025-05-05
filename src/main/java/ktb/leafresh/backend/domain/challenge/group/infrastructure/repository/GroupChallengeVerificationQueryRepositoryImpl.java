package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.domain.entity.QGroupChallengeVerification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GroupChallengeVerificationQueryRepositoryImpl implements GroupChallengeVerificationQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QGroupChallengeVerification gv = QGroupChallengeVerification.groupChallengeVerification;

    @Override
    public List<GroupChallengeVerification> findByChallengeId(Long challengeId, Long cursorId, int size) {
        return queryFactory.selectFrom(gv)
                .where(
                        gv.participantRecord.groupChallenge.id.eq(challengeId),
                        gv.deletedAt.isNull(),
                        ltCursorId(cursorId)
                )
                .orderBy(gv.id.desc())
                .limit(size)
                .fetch();
    }

    private BooleanExpression ltCursorId(Long cursorId) {
        return cursorId != null ? gv.id.lt(cursorId) : null;
    }
}
