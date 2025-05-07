package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.*;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.*;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationHelper;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupChallengeCreatedReadService {

    private final GroupChallengeCreatedQueryRepository createdRepository;

    public GroupChallengeListResponseDto getCreatedChallengesByMember(Long memberId, Long cursorId, int size) {
        List<GroupChallenge> entities = createdRepository.findCreatedByMember(memberId, cursorId, size + 1);

        CursorPaginationResult<GroupChallengeSummaryDto> result =
                CursorPaginationHelper.paginate(entities, size, GroupChallengeSummaryDto::from, GroupChallengeSummaryDto::id);

        return GroupChallengeListResponseDto.builder()
                .groupChallenges(result.items())
                .hasNext(result.hasNext())
                .lastCursorId(result.lastCursorId())
                .build();
    }
}
