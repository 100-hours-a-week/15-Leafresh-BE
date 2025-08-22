package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import ktb.leafresh.backend.domain.challenge.group.presentation.dto.query.GroupChallengeParticipationDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeParticipationCountSummaryDto;

import java.time.LocalDateTime;
import java.util.List;

public interface GroupChallengeParticipationRecordQueryRepository {
  GroupChallengeParticipationCountSummaryDto countParticipationByStatus(Long memberId);

  List<GroupChallengeParticipationDto> findParticipatedByStatus(
      Long memberId, String status, Long cursorId, String cursorTimestamp, int size);
}
