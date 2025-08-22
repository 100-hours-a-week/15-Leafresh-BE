package ktb.leafresh.backend.domain.verification.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationStatCacheService {

  private static final Duration TTL = Duration.ofDays(1);

  private final StringRedisTemplate stringRedisTemplate;
  private final RedisTemplate<String, Object> redisTemplate;

  public void initializeVerificationStats(
      Long verificationId, int viewCount, int likeCount, int commentCount) {
    String key = "verification:stat:" + verificationId;
    stringRedisTemplate.opsForHash().put(key, "viewCount", Integer.toString(viewCount));
    stringRedisTemplate.opsForHash().put(key, "likeCount", Integer.toString(likeCount));
    stringRedisTemplate.opsForHash().put(key, "commentCount", Integer.toString(commentCount));
    redisTemplate.expire(key, TTL);
  }

  public Map<Object, Object> getStats(Long verificationId) {
    String key = VerificationCacheKeys.stat(verificationId);
    return stringRedisTemplate.opsForHash().entries(key);
  }

  public void clearStats(Long verificationId) {
    String key = VerificationCacheKeys.stat(verificationId);
    stringRedisTemplate.delete(key);
    stringRedisTemplate
        .opsForSet()
        .remove(VerificationCacheKeys.dirtySetKey(), verificationId.toString());
    log.info("[VerificationStatCache] 캐시 삭제 - key={}", key);
  }
}
