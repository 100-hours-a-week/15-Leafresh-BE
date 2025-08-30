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

import java.net.InetAddress;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationStatSyncScheduler {

  private static final int MAX_BATCH_SIZE = 100;
  private static final String INSTANCE_ID = generateInstanceId();

  private static String generateInstanceId() {
    try {
      return InetAddress.getLocalHost().getHostName() + "-" + System.currentTimeMillis() % 10000;
    } catch (Exception e) {
      return "unknown-" + System.currentTimeMillis() % 10000;
    }
  }

  private final StringRedisTemplate stringRedisTemplate;
  private final VerificationStatCacheService verificationStatCacheService;
  private final GroupChallengeVerificationRepository verificationRepository;

  /** 5분마다 Redis → DB로 증가분 누적 동기화 */
  @Transactional
  @Scheduled(fixedDelay = 30 * 1000) // 테스트를 위해 30초로 단축
  @SchedulerLock(
      name = "VerificationStatSyncScheduler",
      lockAtLeastFor = "10s",
      lockAtMostFor = "25s")
  public void syncVerificationStats() {
    log.info("[ShedLock] [인스턴스 {}] 통계 동기화 스케줄러 실행 시작!", INSTANCE_ID);
    
    Set<String> dirtyIds =
        stringRedisTemplate
            .opsForSet()
            .distinctRandomMembers(VerificationCacheKeys.dirtySetKey(), MAX_BATCH_SIZE);

    if (dirtyIds == null || dirtyIds.isEmpty()) {
      log.debug("[VerificationStatSyncScheduler] [인스턴스 {}] 동기화 대상 없음", INSTANCE_ID);
      return;
    }

    log.info("[VerificationStatSyncScheduler] [인스턴스 {}] 동기화 시작 - 대상 수: {}", INSTANCE_ID, dirtyIds.size());

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
        log.warn(
            "[VerificationStatSyncScheduler] Redis 조회 실패 - verificationId={}, error={}",
            verificationId,
            redisEx.getMessage(),
            redisEx);
        continue;
      }

      if (stats == null || stats.isEmpty()) {
        verificationStatCacheService.clearStats(verificationId);
        log.debug("[VerificationStatSyncScheduler] 캐시 없음 - verificationId={}", verificationId);
        continue;
      }

      try {
        var before =
            verificationRepository
                .findStatById(verificationId)
                .orElseThrow(
                    () -> new IllegalStateException("존재하지 않는 verificationId: " + verificationId));

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
            view,
            before.getViewCount(),
            before.getViewCount() + view,
            like,
            before.getLikeCount(),
            before.getLikeCount() + like,
            comment,
            before.getCommentCount(),
            before.getCommentCount() + comment);

      } catch (Exception e) {
        log.error(
            "[VerificationStatSyncScheduler] 동기화 실패 - verificationId={}, message={}",
            verificationId,
            e.getMessage(),
            e);
      }
    }

    log.info("[ShedLock] [인스턴스 {}] 통계 동기화 스케줄러 종료", INSTANCE_ID);
  }

  /** 
   * 테스트 비교용: ShedLock 없는 스케줄러 - 5분마다 실행
   * 분산 환경에서 중복 실행됨을 보여주기 위한 용도
   */
  @Scheduled(fixedDelay = 30 * 1000) // 테스트를 위해 30초로 단축
  // @SchedulerLock 주석 처리 = 분산락 없음
  public void syncVerificationStatsWithoutLock() {
    log.info("[NO-LOCK] [인스턴스 {}] 락 없는 통계 동기화 실행!", INSTANCE_ID);
    
    // 간단한 작업 시뮬레이션
    try {
      Thread.sleep(1000); // 1초 작업
      
      // Redis에 실행 기록 저장
      String key = "scheduler:no-lock:execution:" + System.currentTimeMillis();
      stringRedisTemplate.opsForValue().set(key, INSTANCE_ID);
      stringRedisTemplate.expire(key, java.time.Duration.ofMinutes(10));
      
      log.info("[NO-LOCK] [인스턴스 {}] 락 없는 통계 동기화 완료!", INSTANCE_ID);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("[NO-LOCK] [인스턴스 {}] 락 없는 스케줄러 인터럽트", INSTANCE_ID);
    }
  }
}
