package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.*;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.*;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.cache.VerificationStatCacheService;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.LikeRepository;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationHelper;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import ktb.leafresh.backend.global.util.redis.VerificationStatRedisLuaService;
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
public class GroupChallengeVerificationReadService {

    private final GroupChallengeRepository groupChallengeRepository;
    private final GroupChallengeVerificationQueryRepository groupChallengeVerificationQueryRepository;
    private final VerificationStatCacheService verificationStatCacheService;
    private final LikeRepository likeRepository;
    private final VerificationStatRedisLuaService verificationStatRedisLuaService;

    public CursorPaginationResult<GroupChallengeVerificationSummaryDto> getVerifications(
            Long challengeId, Long cursorId, String cursorTimestamp, int size, Long loginMemberId
    ) {
        if (!groupChallengeRepository.existsById(challengeId)) {
            throw new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_NOT_FOUND);
        }

        List<GroupChallengeVerification> verifications =
                groupChallengeVerificationQueryRepository.findByChallengeId(challengeId, cursorId, cursorTimestamp, size + 1);

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
                dto -> dto.id(),
                dto -> dto.createdAt().toLocalDateTime()
        );
    }

    public GroupChallengeRuleResponseDto getChallengeRules(Long challengeId) {
        try {
            GroupChallenge challenge = groupChallengeRepository.findById(challengeId)
                    .orElseThrow(() -> new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_RULE_NOT_FOUND));

            List<GroupChallengeExampleImageDto> exampleImages = challenge.getExampleImages().stream()
                    .map(GroupChallengeExampleImageDto::from)
                    .toList();

            return GroupChallengeRuleResponseDto.of(challenge, exampleImages);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[단체 챌린지 인증 규약 조회 실패] challengeId={}, error={}", challengeId, e.getMessage(), e);
            throw new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_RULE_READ_FAILED);
        }
    }


    public GroupChallengeVerificationDetailResponseDto getVerificationDetail(Long challengeId, Long verificationId, Long loginMemberId) {
        GroupChallengeVerification verification = groupChallengeVerificationQueryRepository
                .findByChallengeIdAndId(challengeId, verificationId)
                .orElseThrow(() -> new CustomException(VerificationErrorCode.VERIFICATION_DETAIL_NOT_FOUND));

        // 캐싱된 통계 조회
        Map<Object, Object> stats = verificationStatCacheService.getStats(verificationId);

        // 조회수 증가 (비회원 포함)
        verificationStatRedisLuaService.increaseVerificationViewCount(verificationId);

        // 좋아요 여부 조회
        Set<Long> likedIds = loginMemberId != null
                ? likeRepository.findLikedVerificationIdsByMemberId(loginMemberId, List.of(verificationId))
                : Set.of();

        boolean isLiked = likedIds.contains(verificationId);

        return GroupChallengeVerificationDetailResponseDto.from(verification, stats, isLiked);
    }
}
