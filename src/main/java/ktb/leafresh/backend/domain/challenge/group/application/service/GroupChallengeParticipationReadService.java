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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupChallengeParticipationReadService {

    private final GroupChallengeParticipationRecordQueryRepository groupChallengeParticipationRecordQueryRepository;
    private final GroupChallengeVerificationQueryRepository groupChallengeVerificationQueryRepository;

    public GroupChallengeParticipationCountResponseDto getParticipationCounts(Long memberId) {
        GroupChallengeParticipationCountSummaryDto summary =
                groupChallengeParticipationRecordQueryRepository.countParticipationByStatus(memberId, LocalDateTime.now());

        return GroupChallengeParticipationCountResponseDto.from(summary);
    }

    public GroupChallengeParticipationListResponseDto getParticipatedChallenges(
            Long memberId, String status, Long cursorId, String cursorTimestamp, int size
    ) {
        List<GroupChallengeParticipationDto> dtos =
                groupChallengeParticipationRecordQueryRepository
                        .findParticipatedByStatus(memberId, status, cursorId, cursorTimestamp, size + 1);

        List<Long> challengeIds = dtos.stream()
                .map(GroupChallengeParticipationDto::getId)
                .toList();

        Map<Long, List<GroupChallengeParticipationSummaryDto.AchievementRecordDto>> achievementRecordMap =
                groupChallengeVerificationQueryRepository.findVerificationsGroupedByChallenge(challengeIds, memberId);

        CursorPaginationResult<GroupChallengeParticipationSummaryDto> page = CursorPaginationHelper.paginateWithTimestamp(
                dtos,
                size,
                dto -> {
                    OffsetDateTime startUtc = OffsetDateTime.of(dto.getStartDate(), ZoneOffset.UTC);
                    OffsetDateTime endUtc = OffsetDateTime.of(dto.getEndDate(), ZoneOffset.UTC);
                    OffsetDateTime createdUtc = OffsetDateTime.of(dto.getCreatedAt(), ZoneOffset.UTC);

                    return GroupChallengeParticipationSummaryDto.of(
                            dto.getId(),
                            dto.getTitle(),
                            dto.getThumbnailUrl(),
                            startUtc,
                            endUtc,
                            dto.getSuccess(),
                            dto.getTotal(),
                            achievementRecordMap.getOrDefault(dto.getId(), List.of()),
                            createdUtc
                    );
                },
                GroupChallengeParticipationSummaryDto::id,
                dto -> dto.createdAt().toLocalDateTime()
        );

        return GroupChallengeParticipationListResponseDto.builder()
                .challenges(page.items())
                .hasNext(page.hasNext())
                .cursorInfo(page.cursorInfo())
                .build();
    }
}
