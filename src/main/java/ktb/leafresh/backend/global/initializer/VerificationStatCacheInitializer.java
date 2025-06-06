package ktb.leafresh.backend.global.initializer;

import jakarta.annotation.PostConstruct;
import ktb.leafresh.backend.domain.verification.infrastructure.cache.VerificationStatCacheService;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.CommentRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerificationStatCacheInitializer {

    private final GroupChallengeVerificationRepository verificationRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final VerificationStatCacheService verificationStatCacheService;
    private final StringRedisTemplate stringRedisTemplate;

    @PostConstruct
    public void initializeVerificationStats() {
        Map<Long, Integer> viewCountMap = new HashMap<>();
        Map<Long, Integer> likeCountMap = new HashMap<>();
        Map<Long, Integer> commentCountMap = new HashMap<>();

        for (Object[] row : verificationRepository.findAllViewCountByVerificationId()) {
            viewCountMap.put((Long) row[0], row[1] != null ? ((Number) row[1]).intValue() : 0);
        }

        for (Object[] row : likeRepository.findAllLikeCountByVerificationId()) {
            likeCountMap.put((Long) row[0], row[1] != null ? ((Number) row[1]).intValue() : 0);
        }

        for (Object[] row : commentRepository.findAllCommentCountByVerificationId()) {
            commentCountMap.put((Long) row[0], row[1] != null ? ((Number) row[1]).intValue() : 0);
        }

        Set<Long> allVerificationIds = new HashSet<>();
        allVerificationIds.addAll(viewCountMap.keySet());
        allVerificationIds.addAll(likeCountMap.keySet());
        allVerificationIds.addAll(commentCountMap.keySet());

        int successCount = 0;
        for (Long verificationId : allVerificationIds) {
            String redisKey = "verification:stat:" + verificationId;

            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(redisKey))) {
                log.info("[VerificationStatCacheInitializer] 기존 Redis 캐시 존재 - 초기화 생략: {}", redisKey);
                continue;
            }

            int views = viewCountMap.getOrDefault(verificationId, 0);
            int likes = likeCountMap.getOrDefault(verificationId, 0);
            int comments = commentCountMap.getOrDefault(verificationId, 0);

            try {
                verificationStatCacheService.initializeVerificationStats(verificationId, views, likes, comments);
                successCount++;
            } catch (Exception e) {
                log.error("[VerificationStatCacheInitializer] 캐시 초기화 실패 - id={}, error={}", verificationId, e.getMessage(), e);
            }
        }

        log.info("[VerificationStatCacheInitializer] Redis 인증 통계 캐시 초기화 완료 - 총 {}건", successCount);
    }
}
