package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.enums.GroupChallengeCategoryName;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.*;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.*;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
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

    public GroupChallengeListResponseDto getGroupChallenges(String input, String category, Long cursorId, int size) {
        if (category == null || category.trim().isEmpty()) {
            throw new CustomException(GlobalErrorCode.INVALID_REQUEST);
        }

        String internalCategoryName = resolveCategoryNameOrThrow(category);

        CursorPaginationResult<GroupChallengeSummaryDto> page = CursorPaginationHelper.paginate(
                searchRepository.findByFilter(input, internalCategoryName, cursorId, size + 1),
                size,
                GroupChallengeSummaryDto::from,
                GroupChallengeSummaryDto::id
        );

        return GroupChallengeListResponseDto.builder()
                .groupChallenges(page.items())
                .hasNext(page.hasNext())
                .lastCursorId(page.lastCursorId())
                .build();
    }

    private String resolveCategoryNameOrThrow(String label) {
        String name = GroupChallengeCategoryName.toEnglish(label);
        if (name == null) {
            throw new CustomException(ChallengeErrorCode.CHALLENGE_CATEGORY_NOT_FOUND);
        }
        return name;
    }
}
