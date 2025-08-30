package ktb.leafresh.backend.global.security;

import io.rebloom.client.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JWT 블랙리스트 서비스 - 성능 메트릭 추적 기능 포함
 * Bloom Filter로 1차 필터링 후 Redis로 2차 정확한 검증하는 2단계 시스템
 */
@Slf4j
@Service("metricsTrackingTokenBlacklistService")
@RequiredArgsConstructor
@Profile("!swagger")
public class MetricsTrackingTokenBlacklistService implements TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private final Client bloomClient;

    @Value("${jwt.bloom-filter.key:accessTokenBlacklist}")
    private String BLOOM_FILTER_NAME;

    // 성능 메트릭 추적용 카운터
    private final AtomicLong totalChecks = new AtomicLong(0);
    private final AtomicLong bloomFilterHits = new AtomicLong(0);
    private final AtomicLong bloomFilterMisses = new AtomicLong(0);
    private final AtomicLong redisChecks = new AtomicLong(0);
    private final AtomicLong actualBlacklistedTokens = new AtomicLong(0);

    @Override
    public void blacklistAccessToken(String accessToken, long expirationTimeMillis) {
        // Bloom Filter에 등록
        bloomClient.add(BLOOM_FILTER_NAME, accessToken);

        // Redis에 TTL과 함께 저장 (정확한 검증용)
        String key = "blacklist:" + accessToken;
        redisTemplate.opsForValue().set(key, "true", expirationTimeMillis, TimeUnit.MILLISECONDS);

        log.info("[블랙리스트 등록] token={}, TTL={}ms", accessToken, expirationTimeMillis);
    }

    @Override
    public boolean isBlacklisted(String accessToken) {
        totalChecks.incrementAndGet();

        // 1단계: Bloom Filter 검사
        boolean mightExist = bloomClient.exists(BLOOM_FILTER_NAME, accessToken);
        
        if (!mightExist) {
            // Bloom Filter에서 확실히 없다고 판단 - Redis 조회 불필요
            bloomFilterMisses.incrementAndGet();
            log.debug("[Bloom Filter Miss] token={} - Redis 조회 생략", accessToken);
            return false;
        }

        // 2단계: Redis에서 정확한 검증 (Bloom Filter에서 존재 가능성 있음)
        bloomFilterHits.incrementAndGet();
        redisChecks.incrementAndGet();
        
        boolean isActuallyBlacklisted = Boolean.TRUE.equals(
            redisTemplate.hasKey("blacklist:" + accessToken)
        );
        
        if (isActuallyBlacklisted) {
            actualBlacklistedTokens.incrementAndGet();
            log.debug("[실제 블랙리스트 토큰 발견] token={}", accessToken);
        } else {
            log.debug("[False Positive] Bloom Filter 오판 - token={}", accessToken);
        }

        return isActuallyBlacklisted;
    }

    /**
     * 성능 메트릭 조회
     */
    public PerformanceMetrics getMetrics() {
        long total = totalChecks.get();
        long redis = redisChecks.get();
        long bloomMiss = bloomFilterMisses.get();
        long bloomHit = bloomFilterHits.get();
        long actualBlacklisted = actualBlacklistedTokens.get();
        
        double redisReductionRate = total > 0 ? (double) bloomMiss / total * 100 : 0;
        double falsePositiveRate = bloomHit > 0 ? (double) (bloomHit - actualBlacklisted) / bloomHit * 100 : 0;

        return PerformanceMetrics.builder()
                .totalChecks(total)
                .bloomFilterHits(bloomHit)
                .bloomFilterMisses(bloomMiss)
                .redisChecks(redis)
                .actualBlacklistedTokens(actualBlacklisted)
                .redisReductionRate(redisReductionRate)
                .falsePositiveRate(falsePositiveRate)
                .build();
    }

    /**
     * 메트릭 초기화
     */
    public void resetMetrics() {
        totalChecks.set(0);
        bloomFilterHits.set(0);
        bloomFilterMisses.set(0);
        redisChecks.set(0);
        actualBlacklistedTokens.set(0);
        log.info("성능 메트릭 초기화 완료");
    }

    /**
     * 성능 메트릭 DTO
     */
    @lombok.Builder
    @lombok.Data
    public static class PerformanceMetrics {
        private long totalChecks;
        private long bloomFilterHits;
        private long bloomFilterMisses;
        private long redisChecks;
        private long actualBlacklistedTokens;
        private double redisReductionRate;
        private double falsePositiveRate;

        @Override
        public String toString() {
            return String.format("""
                🎯 JWT 블랙리스트 성능 메트릭
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                📊 전체 검사: %,d회
                🎯 Bloom Filter Hit: %,d회 (%.1f%%)
                ❌ Bloom Filter Miss: %,d회 (%.1f%%)
                🔍 Redis 조회: %,d회 (%.1f%%)
                ⚠️  실제 블랙리스트: %,d회
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                ✅ Redis 조회 감소율: %.1f%% (Bloom Filter 덕분에)
                📈 False Positive 비율: %.1f%%
                💡 성능 개선: Bloom Filter가 Redis 조회를 %,d회 절약
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                """,
                totalChecks,
                bloomFilterHits, totalChecks > 0 ? (double) bloomFilterHits / totalChecks * 100 : 0,
                bloomFilterMisses, totalChecks > 0 ? (double) bloomFilterMisses / totalChecks * 100 : 0,
                redisChecks, totalChecks > 0 ? (double) redisChecks / totalChecks * 100 : 0,
                actualBlacklistedTokens,
                redisReductionRate,
                falsePositiveRate,
                bloomFilterMisses
            );
        }
    }
}
