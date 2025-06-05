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

    public void increaseViewCount(Long verificationId) {
        increaseStat(verificationId, "viewCount", 1);
    }

    public void increaseLikeCount(Long verificationId) {
        increaseStat(verificationId, "likeCount", 1);
    }

    public void decreaseLikeCount(Long verificationId) {
        increaseStat(verificationId, "likeCount", -1);
    }

    public void increaseCommentCount(Long verificationId) {
        increaseStat(verificationId, "commentCount", 1);
    }

    public void decreaseCommentCount(Long verificationId) {
        increaseStat(verificationId, "commentCount", -1);
    }

    private void increaseStat(Long verificationId, String field, int delta) {
        String key = VerificationCacheKeys.stat(verificationId);
        stringRedisTemplate.opsForHash().increment(key, field, delta);
        stringRedisTemplate.opsForSet().add(VerificationCacheKeys.dirtySetKey(), verificationId.toString());

        // TTL 설정: 매 이벤트마다 갱신 (캐시 자동 정리)
        stringRedisTemplate.expire(key, TTL);

        log.debug("[VerificationStatCache] {}:{} += {}", key, field, delta);
    }

    public Map<Object, Object> getStats(Long verificationId) {
        String key = VerificationCacheKeys.stat(verificationId);
        return stringRedisTemplate.opsForHash().entries(key);
    }

    public void clearStats(Long verificationId) {
        String key = VerificationCacheKeys.stat(verificationId);
        stringRedisTemplate.delete(key);
        stringRedisTemplate.opsForSet().remove(VerificationCacheKeys.dirtySetKey(), verificationId.toString());
        log.info("[VerificationStatCache] 캐시 삭제 - key={}", key);
    }
}
