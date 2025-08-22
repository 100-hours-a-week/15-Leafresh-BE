package ktb.leafresh.backend.global.util.redis;

import ktb.leafresh.backend.domain.verification.infrastructure.cache.VerificationStatCacheService;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@RequiredArgsConstructor
@Slf4j
@Service
public class VerificationStatRedisLuaService {

  private final StringRedisTemplate stringRedisTemplate;
  private final GroupChallengeVerificationRepository groupChallengeVerificationRepository;
  private final VerificationStatCacheService verificationStatCacheService;

  private static final String INCREASE_VIEW_COUNT_LUA =
      """
        local hashKey = KEYS[1]
        local dirtySetKey = KEYS[2]
        local verificationId = ARGV[1]
        local ttlSeconds = tonumber(ARGV[2])

        -- 뷰 카운트 증가
        redis.call("HINCRBY", hashKey, "viewCount", 1)

        -- dirty set에 등록
        redis.call("SADD", dirtySetKey, verificationId)

        -- TTL 설정 (매번 갱신)
        redis.call("EXPIRE", hashKey, ttlSeconds)

        return 1
    """;

  public void increaseVerificationViewCount(Long verificationId) {
    ensureCacheExists(verificationId);

    String statKey = "verification:stat:" + verificationId;
    String dirtySetKey = "verification:stat:dirty";

    try {
      // 조회 전 값 읽기
      String beforeStr = (String) stringRedisTemplate.opsForHash().get(statKey, "viewCount");
      long before = beforeStr != null ? Long.parseLong(beforeStr) : 0;

      // Lua 실행
      stringRedisTemplate.execute(
          new DefaultRedisScript<>(INCREASE_VIEW_COUNT_LUA, Long.class),
          Arrays.asList(statKey, dirtySetKey),
          verificationId.toString(),
          String.valueOf(60 * 60 * 24) // 24시간 TTL
          );

      // 조회 후 값 읽기
      String afterStr = (String) stringRedisTemplate.opsForHash().get(statKey, "viewCount");
      long after = afterStr != null ? Long.parseLong(afterStr) : before;

      log.info("[Lua] 조회수 증가 - verificationId={}, {} → {}", verificationId, before, after);
    } catch (Exception e) {
      log.warn("[Lua] 조회수 증가 실패 - verificationId={}, err={}", verificationId, e.getMessage(), e);
    }
  }

  public void increaseVerificationLikeCount(Long verificationId) {
    ensureCacheExists(verificationId);
    executeLikeCountDelta(verificationId, 1, "좋아요 증가");
  }

  public void decreaseVerificationLikeCount(Long verificationId) {
    ensureCacheExists(verificationId);
    executeLikeCountDelta(verificationId, -1, "좋아요 감소");
  }

  private void executeLikeCountDelta(Long verificationId, int delta, String actionLabel) {
    String statKey = "verification:stat:" + verificationId;
    String dirtySetKey = "verification:stat:dirty";

    String script =
        """
        local hashKey = KEYS[1]
        local dirtySetKey = KEYS[2]
        local verificationId = ARGV[1]
        local delta = tonumber(ARGV[2])
        local ttlSeconds = tonumber(ARGV[3])

        redis.call("HINCRBY", hashKey, "likeCount", delta)
        redis.call("SADD", dirtySetKey, verificationId)
        redis.call("EXPIRE", hashKey, ttlSeconds)

        return 1
    """;

    try {
      stringRedisTemplate.execute(
          new DefaultRedisScript<>(script, Long.class),
          Arrays.asList(statKey, dirtySetKey),
          verificationId.toString(),
          String.valueOf(delta),
          String.valueOf(60 * 60 * 24));
      log.debug("[Lua] {} 완료 - verificationId={}, delta={}", actionLabel, verificationId, delta);
    } catch (Exception e) {
      log.warn(
          "[Lua] {} 실패 - verificationId={}, err={}",
          actionLabel,
          verificationId,
          e.getMessage(),
          e);
    }
  }

  public void increaseVerificationCommentCount(Long verificationId) {
    ensureCacheExists(verificationId);
    executeCommentCountDelta(verificationId, 1, "댓글 수 증가");
  }

  public void decreaseVerificationCommentCount(Long verificationId) {
    ensureCacheExists(verificationId);
    executeCommentCountDelta(verificationId, -1, "댓글 수 감소");
  }

  private void executeCommentCountDelta(Long verificationId, int delta, String actionLabel) {
    String statKey = "verification:stat:" + verificationId;
    String dirtySetKey = "verification:stat:dirty";

    String script =
        """
        local hashKey = KEYS[1]
        local dirtySetKey = KEYS[2]
        local verificationId = ARGV[1]
        local delta = tonumber(ARGV[2])
        local ttlSeconds = tonumber(ARGV[3])

        redis.call("HINCRBY", hashKey, "commentCount", delta)
        redis.call("SADD", dirtySetKey, verificationId)
        redis.call("EXPIRE", hashKey, ttlSeconds)

        return 1
    """;

    try {
      stringRedisTemplate.execute(
          new DefaultRedisScript<>(script, Long.class),
          Arrays.asList(statKey, dirtySetKey),
          verificationId.toString(),
          String.valueOf(delta),
          String.valueOf(60 * 60 * 24));
      log.debug("[Lua] {} 완료 - verificationId={}, delta={}", actionLabel, verificationId, delta);
    } catch (Exception e) {
      log.warn(
          "[Lua] {} 실패 - verificationId={}, err={}",
          actionLabel,
          verificationId,
          e.getMessage(),
          e);
    }
  }

  private void ensureCacheExists(Long verificationId) {
    String statKey = "verification:stat:" + verificationId;
    Boolean exists = stringRedisTemplate.hasKey(statKey);

    if (Boolean.FALSE.equals(exists)) {
      var stat =
          groupChallengeVerificationRepository
              .findStatById(verificationId)
              .orElseThrow(
                  () -> new IllegalStateException("verificationId not found: " + verificationId));

      verificationStatCacheService.initializeVerificationStats(
          verificationId, stat.getViewCount(), stat.getLikeCount(), stat.getCommentCount());

      log.info("[Redis 복구] 캐시 누락으로 인해 Redis 재초기화 - verificationId={}", verificationId);
    }
  }
}
