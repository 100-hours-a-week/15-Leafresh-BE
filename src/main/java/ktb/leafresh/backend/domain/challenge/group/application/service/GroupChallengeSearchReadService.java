package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.enums.GroupChallengeCategoryName;
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
public class GroupChallengeSearchReadService {

    private final GroupChallengeSearchQueryRepository searchRepository;

    public CursorPaginationResult<GroupChallengeSummaryDto> getGroupChallenges(
            String input, GroupChallengeCategoryName category, Long cursorId, String cursorTimestamp, int size) {

        String internalCategoryName = (category != null) ? category.name() : null;

        List<GroupChallenge> challenges = searchRepository
                .findByFilter(input, internalCategoryName, cursorId, cursorTimestamp, size + 1);

        return CursorPaginationHelper.paginateWithTimestamp(
                challenges,
                size,
                GroupChallengeSummaryDto::from,
                GroupChallengeSummaryDto::id,
                GroupChallengeSummaryDto::createdAt
        );
    }
}
