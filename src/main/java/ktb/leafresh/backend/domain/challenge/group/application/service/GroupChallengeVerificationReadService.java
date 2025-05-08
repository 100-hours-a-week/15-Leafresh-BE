package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.*;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.*;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
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

    public CursorPaginationResult<GroupChallengeVerificationSummaryDto> getVerifications(
            Long challengeId, Long cursorId, String cursorTimestamp, int size
    ) {
        List<GroupChallengeVerification> entities =
                groupChallengeVerificationQueryRepository.findByChallengeId(challengeId, cursorId, cursorTimestamp, size + 1);

        return CursorPaginationHelper.paginateWithTimestamp(
                entities,
                size,
                GroupChallengeVerificationSummaryDto::from,
                GroupChallengeVerificationSummaryDto::id,
                GroupChallengeVerificationSummaryDto::createdAt
        );
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
