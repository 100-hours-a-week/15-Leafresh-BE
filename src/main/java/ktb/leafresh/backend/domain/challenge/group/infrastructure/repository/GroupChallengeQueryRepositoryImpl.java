package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.QGroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.QGroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeParticipationCountSummaryDto;
import ktb.leafresh.backend.global.common.entity.enums.ParticipantStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static ktb.leafresh.backend.global.common.entity.enums.ParticipantStatus.*;

@Repository
@RequiredArgsConstructor
public class GroupChallengeQueryRepositoryImpl implements GroupChallengeQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QGroupChallenge gc = QGroupChallenge.groupChallenge;
    private final QGroupChallengeParticipantRecord pr = QGroupChallengeParticipantRecord.groupChallengeParticipantRecord;

    @Override
    public List<GroupChallenge> findByFilter(String input, String category, Long cursorId, int size) {
        return queryFactory.selectFrom(gc)
                .where(
                        gc.deletedAt.isNull(),
                        gc.endDate.goe(LocalDateTime.now()),
                        likeInput(input),
                        eqCategory(category),
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

    private BooleanExpression eqCategory(String category) {
        return category != null ? gc.category.name.eq(category) : null;
    }

    private BooleanExpression ltCursorId(Long cursorId) {
        return cursorId != null ? gc.id.lt(cursorId) : null;
    }

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

    @Override
    public GroupChallengeParticipationCountSummaryDto countParticipationByStatus(Long memberId, LocalDateTime now) {
        List<GroupChallengeParticipantRecord> records = queryFactory
                .selectFrom(pr)
                .join(pr.groupChallenge, gc).fetchJoin()
                .where(
                        pr.member.id.eq(memberId),
                        pr.status.in(ACTIVE, FINISHED, WAITING), // 유효 참여 상태만 포함
                        pr.deletedAt.isNull(),
                        gc.deletedAt.isNull()
                )
                .fetch();

        int notStarted = 0;
        int ongoing = 0;
        int completed = 0;

        for (GroupChallengeParticipantRecord record : records) {
            GroupChallenge challenge = record.getGroupChallenge();
            ParticipantStatus status = record.getStatus();

            if (status == FINISHED) {
                completed++;
            } else if (now.isBefore(challenge.getStartDate())) {
                notStarted++;
            } else if (now.isAfter(challenge.getEndDate())) {
                completed++;
            } else {
                ongoing++;
            }
        }

        return new GroupChallengeParticipationCountSummaryDto(notStarted, ongoing, completed);
    }
}
