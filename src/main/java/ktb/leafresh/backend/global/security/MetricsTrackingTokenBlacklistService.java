package ktb.leafresh.backend.global.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JWT 블랙리스트 서비스 - 성능 메트릭 추적 기능 포함
 * Bloom Filter로 1차 필터링 후 Redis로 2차 정확한 검증하는 2단계 시스템
 * False Positive 확률: 0.01% (application.yml 설정)
 */
@Slf4j
@Service("metricsTrackingTokenBlacklistService")
@RequiredArgsConstructor
@Profile("!swagger")
public class MetricsTrackingTokenBlacklistService implements TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private final BloomFilterService bloomFilterService;

    // 성능 메트릭 추적용 카운터
    private final AtomicLong totalChecks = new AtomicLong(0);
    private final AtomicLong bloomFilterHits = new AtomicLong(0);
    private final AtomicLong bloomFilterMisses = new AtomicLong(0);
    private final AtomicLong redisChecks = new AtomicLong(0);
    private final AtomicLong actualBlacklistedTokens = new AtomicLong(0);

    @Override
    public void blacklistAccessToken(String accessToken, long expirationTimeMillis) {
        // Bloom Filter에 등록 (False Positive 확률: 0.01%)
        bloomFilterService.addToken(accessToken);

        // Redis에 TTL과 함께 저장 (정확한 검증용)
        String key = "blacklist:" + accessToken;
        redisTemplate.opsForValue().set(key, "true", expirationTimeMillis, TimeUnit.MILLISECONDS);

        log.info("[블랙리스트 등록] token={}, TTL={}ms, falsePositiveProbability={}%", 
            accessToken, expirationTimeMillis, bloomFilterService.getFalsePositiveProbability() * 100);
    }

    @Override
    public boolean isBlacklisted(String accessToken) {
        totalChecks.incrementAndGet();

        // 1단계: Bloom Filter 검사 (False Positive 확률: 0.01%)
        boolean mightExist = bloomFilterService.mightContain(accessToken);
        
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
            log.debug("[False Positive] Bloom Filter 오판 - token={}, 설정 확률: {}%", 
                accessToken, bloomFilterService.getFalsePositiveProbability() * 100);
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
                .expectedInsertions(bloomFilterService.getExpectedInsertions())
                .configuredFalsePositiveProbability(bloomFilterService.getFalsePositiveProbability())
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
        private long expectedInsertions;
        private double configuredFalsePositiveProbability;

        @Override
        public String toString() {
            return String.format("""
                🎯 JWT 블랙리스트 성능 메트릭
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                ⚙️  Bloom Filter 설정:
                   - 예상 삽입 수: %,d개
                   - 설정 False Positive 확률: %.4f%% (1만회 중 %.0f회)
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                📊 실제 성능 지표:
                   - 전체 검사: %,d회
                   - Bloom Filter Hit: %,d회 (%.1f%%)
                   - Bloom Filter Miss: %,d회 (%.1f%%)
                   - Redis 조회: %,d회 (%.1f%%)
                   - 실제 블랙리스트: %,d회
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                📈 성능 개선 결과:
                   - Redis 조회 감소율: %.1f%% (Bloom Filter 덕분에)
                   - 실제 False Positive 비율: %.4f%%
                   - 설정 대비 오차: %.4f%% (설정 %.4f%% vs 실제 %.4f%%)
                   - 절약된 Redis 조회: %,d회
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                """,
                expectedInsertions,
                configuredFalsePositiveProbability * 100, configuredFalsePositiveProbability * 10000,
                totalChecks,
                bloomFilterHits, totalChecks > 0 ? (double) bloomFilterHits / totalChecks * 100 : 0,
                bloomFilterMisses, totalChecks > 0 ? (double) bloomFilterMisses / totalChecks * 100 : 0,
                redisChecks, totalChecks > 0 ? (double) redisChecks / totalChecks * 100 : 0,
                actualBlacklistedTokens,
                redisReductionRate,
                falsePositiveRate,
                Math.abs(falsePositiveRate - configuredFalsePositiveProbability * 100),
                configuredFalsePositiveProbability * 100, falsePositiveRate,
                bloomFilterMisses
            );
        }
    }
}
