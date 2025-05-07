package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.*;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.query.GroupChallengeParticipationDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.*;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationHelper;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupChallengeParticipationReadService {

    private final GroupChallengeParticipationRecordQueryRepository groupChallengeParticipationRecordQueryRepository;

    public GroupChallengeParticipationCountResponseDto getParticipationCounts(Long memberId) {
        GroupChallengeParticipationCountSummaryDto summary =
                groupChallengeParticipationRecordQueryRepository.countParticipationByStatus(memberId, LocalDateTime.now());

        return GroupChallengeParticipationCountResponseDto.from(summary);
    }

    public GroupChallengeParticipationListResponseDto getParticipatedChallenges(
            Long memberId, String status, Long cursorId, int size
    ) {
        List<GroupChallengeParticipationDto> dtos =
                groupChallengeParticipationRecordQueryRepository.findParticipatedByStatus(memberId, status, cursorId, size + 1);

        CursorPaginationResult<GroupChallengeParticipationSummaryDto> page = CursorPaginationHelper.paginate(
                dtos,
                size,
                dto -> GroupChallengeParticipationSummaryDto.of(
                        dto.getId(),
                        dto.getTitle(),
                        dto.getThumbnailUrl(),
                        dto.getStartDate(),
                        dto.getEndDate(),
                        dto.getSuccess(),
                        dto.getTotal()
                ),
                GroupChallengeParticipationSummaryDto::id
        );

        return GroupChallengeParticipationListResponseDto.builder()
                .challenges(page.items())
                .hasNext(page.hasNext())
                .lastCursorId(page.lastCursorId())
                .build();
    }
}
