package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.*;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.*;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
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
public class GroupChallengeVerificationReadService {

    private final GroupChallengeRepository groupChallengeRepository;
    private final GroupChallengeVerificationQueryRepository groupChallengeVerificationQueryRepository;

    public GroupChallengeVerificationListResponseDto getVerifications(
            Long challengeId, Long cursorId, int size
    ) {
        CursorPaginationResult<GroupChallengeVerificationSummaryDto> page = CursorPaginationHelper.paginate(
                groupChallengeVerificationQueryRepository.findByChallengeId(challengeId, cursorId, size + 1),
                size,
                GroupChallengeVerificationSummaryDto::from,
                GroupChallengeVerificationSummaryDto::id
        );

        return GroupChallengeVerificationListResponseDto.builder()
                .verifications(page.items())
                .hasNext(page.hasNext())
                .lastCursorId(page.lastCursorId())
                .build();
    }

    public GroupChallengeRuleResponseDto getChallengeRules(Long challengeId) {
        GroupChallenge challenge = groupChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_NOT_FOUND));

        List<GroupChallengeExampleImageDto> exampleImages = challenge.getExampleImages().stream()
                .map(GroupChallengeExampleImageDto::from)
                .toList();

        return GroupChallengeRuleResponseDto.of(challenge, exampleImages);
    }
}
