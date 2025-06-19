package ktb.leafresh.backend.domain.verification.infrastructure.scheduler;

import jakarta.transaction.Transactional;
import ktb.leafresh.backend.domain.verification.infrastructure.cache.VerificationCacheKeys;
import ktb.leafresh.backend.domain.verification.infrastructure.cache.VerificationStatCacheService;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationStatSyncScheduler {

    private static final int MAX_BATCH_SIZE = 100;

    private final StringRedisTemplate stringRedisTemplate;
    private final VerificationStatCacheService verificationStatCacheService;
    private final GroupChallengeVerificationRepository verificationRepository;

    /**
     * 5분마다 Redis → DB로 증가분 누적 동기화
     */
    @Transactional
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    @SchedulerLock(name = "VerificationStatSyncScheduler", lockAtLeastFor = "1m", lockAtMostFor = "10m")
    public void syncVerificationStats() {
        Set<String> dirtyIds = stringRedisTemplate.opsForSet()
                .distinctRandomMembers(VerificationCacheKeys.dirtySetKey(), MAX_BATCH_SIZE);

        if (dirtyIds == null || dirtyIds.isEmpty()) {
            log.debug("[VerificationStatSyncScheduler] 동기화 대상 없음");
            return;
        }

        log.info("[VerificationStatSyncScheduler] 동기화 시작 - 대상 수: {}", dirtyIds.size());

        for (String idStr : dirtyIds) {
            Long verificationId;
            try {
                verificationId = Long.valueOf(idStr);
            } catch (NumberFormatException e) {
                log.warn("[VerificationStatSyncScheduler] 잘못된 ID 형식: {}", idStr);
                continue;
            }

            Map<Object, Object> stats;
            try {
                stats = verificationStatCacheService.getStats(verificationId);
            } catch (Exception redisEx) {
                log.warn("[VerificationStatSyncScheduler] Redis 조회 실패 - verificationId={}, error={}",
                        verificationId, redisEx.getMessage(), redisEx);
                continue;
            }

            if (stats == null || stats.isEmpty()) {
                verificationStatCacheService.clearStats(verificationId);
                log.debug("[VerificationStatSyncScheduler] 캐시 없음 - verificationId={}", verificationId);
                continue;
            }

            try {
                var before = verificationRepository.findStatById(verificationId)
                        .orElseThrow(() -> new IllegalStateException("존재하지 않는 verificationId: " + verificationId));

                int view = Integer.parseInt(stats.getOrDefault("viewCount", "0").toString());
                int like = Integer.parseInt(stats.getOrDefault("likeCount", "0").toString());
                int comment = Integer.parseInt(stats.getOrDefault("commentCount", "0").toString());

                int deltaView = view - before.getViewCount();
                int deltaLike = like - before.getLikeCount();
                int deltaComment = comment - before.getCommentCount();

                if (deltaView == 0 && deltaLike == 0 && deltaComment == 0) {
                    verificationStatCacheService.clearStats(verificationId);
                    continue;
                }

                verificationRepository.increaseCounts(verificationId, deltaView, deltaLike, deltaComment);

                verificationStatCacheService.clearStats(verificationId);

                log.info(
                        "[VerificationStatSyncScheduler] 동기화 완료 - verificationId={} | view(+{}): {} → {}, like(+{}): {} → {}, comment(+{}): {} → {}",
                        verificationId,
                        view, before.getViewCount(), before.getViewCount() + view,
                        like, before.getLikeCount(), before.getLikeCount() + like,
                        comment, before.getCommentCount(), before.getCommentCount() + comment
                );

            } catch (Exception e) {
                log.error("[VerificationStatSyncScheduler] 동기화 실패 - verificationId={}, message={}",
                        verificationId, e.getMessage(), e);
            }
        }

        log.info("[VerificationStatSyncScheduler] 동기화 종료");
    }
}
