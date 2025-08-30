package ktb.leafresh.backend.global.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 순수 Redis 기반 JWT 블랙리스트 서비스 - 비교 테스트용
 * Bloom Filter 없이 모든 요청을 Redis로 직접 조회하는 방식
 */
@Slf4j
@Service("pureRedisTokenBlacklistService")
@RequiredArgsConstructor
@Profile("!swagger")
public class PureRedisTokenBlacklistService implements TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    // 성능 메트릭 추적용 카운터
    private final AtomicLong totalChecks = new AtomicLong(0);
    private final AtomicLong redisChecks = new AtomicLong(0);
    private final AtomicLong actualBlacklistedTokens = new AtomicLong(0);

    @Override
    public void blacklistAccessToken(String accessToken, long expirationTimeMillis) {
        // Redis에만 저장 (Bloom Filter 사용 안함)
        String key = "blacklist:" + accessToken;
        redisTemplate.opsForValue().set(key, "true", expirationTimeMillis, TimeUnit.MILLISECONDS);

        log.info("[블랙리스트 등록 - Pure Redis] token={}, TTL={}ms", accessToken, expirationTimeMillis);
    }

    @Override
    public boolean isBlacklisted(String accessToken) {
        totalChecks.incrementAndGet();
        redisChecks.incrementAndGet();

        // 모든 요청을 Redis로 직접 조회
        boolean isBlacklisted = Boolean.TRUE.equals(
            redisTemplate.hasKey("blacklist:" + accessToken)
        );

        if (isBlacklisted) {
            actualBlacklistedTokens.incrementAndGet();
            log.debug("[Pure Redis - 블랙리스트 토큰 발견] token={}", accessToken);
        } else {
            log.debug("[Pure Redis - 정상 토큰] token={}", accessToken);
        }

        return isBlacklisted;
    }

    /**
     * 성능 메트릭 조회
     */
    public PureRedisMetrics getMetrics() {
        long total = totalChecks.get();
        long redis = redisChecks.get();
        long actualBlacklisted = actualBlacklistedTokens.get();

        return PureRedisMetrics.builder()
                .totalChecks(total)
                .redisChecks(redis)
                .actualBlacklistedTokens(actualBlacklisted)
                .build();
    }

    /**
     * 메트릭 초기화
     */
    public void resetMetrics() {
        totalChecks.set(0);
        redisChecks.set(0);
        actualBlacklistedTokens.set(0);
        log.info("Pure Redis 성능 메트릭 초기화 완료");
    }

    /**
     * 순수 Redis 성능 메트릭 DTO
     */
    @lombok.Builder
    @lombok.Data
    public static class PureRedisMetrics {
        private long totalChecks;
        private long redisChecks;
        private long actualBlacklistedTokens;

        @Override
        public String toString() {
            return String.format("""
                📊 Pure Redis 블랙리스트 성능 메트릭
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                📊 전체 검사: %,d회
                🔍 Redis 조회: %,d회 (100%% - 모든 요청)
                ⚠️  실제 블랙리스트: %,d회
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                💡 모든 토큰 검사마다 Redis 조회 필요
                🔥 Bloom Filter 없이는 캐시 효율성 제한적
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                """,
                totalChecks,
                redisChecks,
                actualBlacklistedTokens
            );
        }
    }
}
