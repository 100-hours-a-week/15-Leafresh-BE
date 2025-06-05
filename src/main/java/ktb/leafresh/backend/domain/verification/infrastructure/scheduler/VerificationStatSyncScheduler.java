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

    private final StringRedisTemplate stringRedisTemplate;
    private final VerificationStatCacheService verificationStatCacheService;
    private final GroupChallengeVerificationRepository verificationRepository;

    /**
     * 5분마다 Redis → DB로 누적 카운트 동기화
     */
    @Transactional
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    @SchedulerLock(name = "VerificationStatSyncScheduler", lockAtLeastFor = "1m", lockAtMostFor = "10m")
    public void syncVerificationStats() {
        Set<String> dirtyIds = stringRedisTemplate.opsForSet().members(VerificationCacheKeys.dirtySetKey());

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
                log.warn("[VerificationStatSyncScheduler] Redis 장애로 캐시 조회 실패 - verificationId={}, err={}",
                        verificationId, redisEx.getMessage(), redisEx);
                continue;
            }

            if (stats == null || stats.isEmpty()) {
                verificationStatCacheService.clearStats(verificationId);
                continue;
            }

            try {
                int view = Integer.parseInt(stats.getOrDefault("viewCount", "0").toString());
                int like = Integer.parseInt(stats.getOrDefault("likeCount", "0").toString());
                int comment = Integer.parseInt(stats.getOrDefault("commentCount", "0").toString());

                if (view == 0 && like == 0 && comment == 0) {
                    verificationStatCacheService.clearStats(verificationId);
                    continue;
                }

                verificationRepository.updateCounts(verificationId, view, like, comment);
                verificationStatCacheService.clearStats(verificationId);

                log.info("[VerificationStatSyncScheduler] 동기화 완료 - verificationId={}, +views={}, +likes={}, +comments={}",
                        verificationId, view, like, comment);

            } catch (Exception e) {
                log.error("[VerificationStatSyncScheduler] 동기화 실패 - verificationId={}, message={}",
                        verificationId, e.getMessage(), e);
            }
        }

        log.info("[VerificationStatSyncScheduler] 동기화 종료");
    }
}
