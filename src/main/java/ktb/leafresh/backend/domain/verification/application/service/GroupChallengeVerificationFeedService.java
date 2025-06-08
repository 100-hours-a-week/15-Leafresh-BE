package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeVerificationSummaryDto;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.cache.VerificationStatCacheService;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationFeedQueryRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.LikeRepository;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationHelper;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupChallengeVerificationFeedService {

    private final GroupChallengeVerificationFeedQueryRepository feedQueryRepository;
    private final VerificationStatCacheService verificationStatCacheService;
    private final LikeRepository likeRepository;

    public CursorPaginationResult<GroupChallengeVerificationSummaryDto> getGroupChallengeVerifications(
            Long cursorId,
            String cursorTimestamp,
            int size,
            String category,
            Long loginMemberId
    ) {
        List<GroupChallengeVerification> verifications =
                feedQueryRepository.findAllByFilter(category, cursorId, cursorTimestamp, size + 1);

        List<Long> verificationIds = verifications.stream()
                .map(GroupChallengeVerification::getId)
                .toList();

        Map<Long, Map<Object, Object>> redisStats = verificationIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> verificationStatCacheService.getStats(id)
                ));

        Set<Long> likedIds = loginMemberId != null
                ? likeRepository.findLikedVerificationIdsByMemberId(loginMemberId, verificationIds)
                : Set.of();

        return CursorPaginationHelper.paginateWithTimestamp(
                verifications,
                size,
                v -> GroupChallengeVerificationSummaryDto.from(
                        v,
                        redisStats.get(v.getId()),
                        likedIds.contains(v.getId())
                ),
                GroupChallengeVerificationSummaryDto::id,
                GroupChallengeVerificationSummaryDto::createdAt
        );
    }
}
