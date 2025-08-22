package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.QGroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.QGroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.query.GroupChallengeParticipationDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeParticipationCountSummaryDto;
import ktb.leafresh.backend.domain.verification.domain.entity.QGroupChallengeVerification;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.common.entity.enums.ParticipantStatus;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.util.pagination.CursorConditionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static ktb.leafresh.backend.global.common.entity.enums.ParticipantStatus.*;

@Repository
@RequiredArgsConstructor
public class GroupChallengeParticipationRecordQueryRepositoryImpl
    implements GroupChallengeParticipationRecordQueryRepository {

  private final JPAQueryFactory queryFactory;
  private final QGroupChallenge challenge = QGroupChallenge.groupChallenge;
  private final QGroupChallengeParticipantRecord record =
      QGroupChallengeParticipantRecord.groupChallengeParticipantRecord;
  private final QGroupChallengeVerification verification =
      QGroupChallengeVerification.groupChallengeVerification;

  @Override
  public GroupChallengeParticipationCountSummaryDto countParticipationByStatus(Long memberId) {
    int notStarted = countByStatus(memberId, "not_started");
    int ongoing = countByStatus(memberId, "ongoing");
    int completed = countByStatus(memberId, "completed");

    return new GroupChallengeParticipationCountSummaryDto(notStarted, ongoing, completed);
  }

  private int countByStatus(Long memberId, String status) {
    LocalDateTime utcNow = LocalDateTime.now(ZoneOffset.UTC);
    Long count =
        queryFactory
            .select(record.count())
            .from(record)
            .join(record.groupChallenge, challenge)
            .where(
                record.member.id.eq(memberId),
                record.status.in(ACTIVE, FINISHED, WAITING),
                record.deletedAt.isNull(),
                challenge.deletedAt.isNull(),
                applyStatusFilter(status, utcNow))
            .fetchOne();

    return count != null ? count.intValue() : 0;
  }

  @Override
  public List<GroupChallengeParticipationDto> findParticipatedByStatus(
      Long memberId, String status, Long cursorId, String cursorTimestamp, int size) {
    LocalDateTime ts = CursorConditionUtils.parseTimestamp(cursorTimestamp);

    return queryFactory
        .select(
            Projections.constructor(
                GroupChallengeParticipationDto.class,
                challenge.id,
                challenge.title,
                challenge.imageUrl,
                challenge.startDate,
                challenge.endDate,
                ExpressionUtils.as(
                    JPAExpressions.select(verification.count())
                        .from(verification)
                        .where(
                            verification.participantRecord.eq(record),
                            verification.status.eq(ChallengeStatus.SUCCESS)),
                    "success"),
                ExpressionUtils.as(
                    JPAExpressions.select(
                        Expressions.numberTemplate(
                            Long.class,
                            "DATEDIFF({0}, {1}) + 1",
                            challenge.endDate,
                            record.createdAt)),
                    "total"),
                challenge.createdAt))
        .from(record)
        .join(record.groupChallenge, challenge)
        .where(
            record.member.id.eq(memberId),
            record.deletedAt.isNull(),
            challenge.deletedAt.isNull(),
            applyStatusFilter(status, LocalDateTime.now(ZoneOffset.UTC)),
            CursorConditionUtils.ltCursorWithTimestamp(
                challenge.createdAt, challenge.id, ts, cursorId))
        .orderBy(challenge.createdAt.desc(), challenge.id.desc())
        .limit(size + 1)
        .fetch();
  }

  private BooleanExpression applyStatusFilter(String status, LocalDateTime now) {
    BooleanExpression hasAnyVerification =
        record.id.in(
            JPAExpressions.select(verification.participantRecord.id)
                .from(verification)
                .where(
                    verification.participantRecord.id.eq(record.id),
                    verification.deletedAt.isNull(),
                    verification.status.in(
                        ChallengeStatus.PENDING_APPROVAL,
                        ChallengeStatus.SUCCESS,
                        ChallengeStatus.FAILURE)));

    return switch (status.toLowerCase()) {
      case "not_started" ->
          challenge
              .startDate
              .loe(now)
              .and(challenge.endDate.goe(now)) // 기간 중
              .and(hasAnyVerification.not()); // 인증 이력 없음

      case "ongoing" ->
          challenge
              .startDate
              .loe(now)
              .and(challenge.endDate.goe(now)) // 기간 중
              .and(hasAnyVerification); // 인증 이력 존재

      case "completed" ->
          challenge
              .endDate
              .lt(now) // 기간 종료
              .or(record.status.eq(FINISHED)); // 수동 종료

      default -> throw new CustomException(GlobalErrorCode.INVALID_REQUEST);
    };
  }
}
