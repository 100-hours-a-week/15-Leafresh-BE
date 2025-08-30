package ktb.leafresh.backend.domain.auth.presentation.controller;

import ktb.leafresh.backend.global.security.MetricsTrackingTokenBlacklistService;
import ktb.leafresh.backend.global.security.PureRedisTokenBlacklistService;
import ktb.leafresh.backend.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * JWT 블랙리스트 성능 테스트 컨트롤러
 * Bloom Filter vs Pure Redis 성능 비교 테스트
 */
@RestController
@RequestMapping("/api/test/blacklist-performance")
@RequiredArgsConstructor
@Slf4j
@Profile("!swagger")
public class TokenBlacklistPerformanceController {

    @Qualifier("metricsTrackingTokenBlacklistService")
    private final MetricsTrackingTokenBlacklistService bloomFilterService;

    @Qualifier("pureRedisTokenBlacklistService")
    private final PureRedisTokenBlacklistService pureRedisService;

    private final TokenProvider tokenProvider;

    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    /**
     * 성능 비교 테스트 실행
     */
    @PostMapping("/run-comparison-test")
    public PerformanceTestResult runPerformanceComparisonTest(
            @RequestParam(defaultValue = "1000") int blacklistedTokenCount,
            @RequestParam(defaultValue = "10000") int totalTestTokenCount,
            @RequestParam(defaultValue = "10") int concurrentThreads) {

        log.info("🚀 JWT 블랙리스트 성능 비교 테스트 시작");
        log.info("📊 블랙리스트 토큰 수: {}", blacklistedTokenCount);
        log.info("📊 총 테스트 토큰 수: {}", totalTestTokenCount);
        log.info("📊 동시 스레드 수: {}", concurrentThreads);

        // 메트릭 초기화
        bloomFilterService.resetMetrics();
        pureRedisService.resetMetrics();

        // 1단계: 테스트 데이터 준비
        TestData testData = prepareTestData(blacklistedTokenCount, totalTestTokenCount);
        log.info("✅ 테스트 데이터 준비 완료");

        // 2단계: Bloom Filter + Redis 테스트
        long bloomFilterStartTime = System.currentTimeMillis();
        runBloomFilterTest(testData, concurrentThreads);
        long bloomFilterDuration = System.currentTimeMillis() - bloomFilterStartTime;
        var bloomFilterMetrics = bloomFilterService.getMetrics();

        log.info("✅ Bloom Filter + Redis 테스트 완료 ({}ms)", bloomFilterDuration);

        // 3단계: Pure Redis 테스트
        long pureRedisStartTime = System.currentTimeMillis();
        runPureRedisTest(testData, concurrentThreads);
        long pureRedisDuration = System.currentTimeMillis() - pureRedisStartTime;
        var pureRedisMetrics = pureRedisService.getMetrics();

        log.info("✅ Pure Redis 테스트 완료 ({}ms)", pureRedisDuration);

        // 4단계: 결과 분석
        return PerformanceTestResult.builder()
                .testConfiguration(TestConfiguration.builder()
                        .blacklistedTokenCount(blacklistedTokenCount)
                        .totalTestTokenCount(totalTestTokenCount)
                        .concurrentThreads(concurrentThreads)
                        .testDateTime(LocalDateTime.now())
                        .build())
                .bloomFilterResult(BloomFilterTestResult.builder()
                        .executionTimeMs(bloomFilterDuration)
                        .metrics(bloomFilterMetrics)
                        .build())
                .pureRedisResult(PureRedisTestResult.builder()
                        .executionTimeMs(pureRedisDuration)
                        .metrics(pureRedisMetrics)
                        .build())
                .comparison(PerformanceComparison.builder()
                        .speedImprovementPercent(
                            pureRedisDuration > 0 ? 
                            (double)(pureRedisDuration - bloomFilterDuration) / pureRedisDuration * 100 : 0)
                        .redisQueryReduction(bloomFilterMetrics.getBloomFilterMisses())
                        .redisQueryReductionPercent(bloomFilterMetrics.getRedisReductionRate())
                        .build())
                .build();
    }

    /**
     * 테스트 데이터 준비
     */
    private TestData prepareTestData(int blacklistedCount, int totalCount) {
        List<String> blacklistedTokens = new ArrayList<>();
        List<String> allTestTokens = new ArrayList<>();

        // 블랙리스트에 추가할 토큰 생성 및 등록
        for (int i = 0; i < blacklistedCount; i++) {
            String token = generateTestToken();
            blacklistedTokens.add(token);
            allTestTokens.add(token);

            // 두 서비스 모두에 블랙리스트 등록
            long expirationTime = 3600000L; // 1시간
            bloomFilterService.blacklistAccessToken(token, expirationTime);
            pureRedisService.blacklistAccessToken(token, expirationTime);
        }

        // 추가 정상 토큰들 생성
        for (int i = blacklistedCount; i < totalCount; i++) {
            allTestTokens.add(generateTestToken());
        }

        return TestData.builder()
                .blacklistedTokens(blacklistedTokens)
                .allTestTokens(allTestTokens)
                .build();
    }

    /**
     * Bloom Filter + Redis 테스트 실행
     */
    private void runBloomFilterTest(TestData testData, int concurrentThreads) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        int tokensPerThread = testData.getAllTestTokens().size() / concurrentThreads;

        for (int i = 0; i < concurrentThreads; i++) {
            final int startIndex = i * tokensPerThread;
            final int endIndex = (i == concurrentThreads - 1) ? 
                testData.getAllTestTokens().size() : (i + 1) * tokensPerThread;

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = startIndex; j < endIndex; j++) {
                    bloomFilterService.isBlacklisted(testData.getAllTestTokens().get(j));
                }
            }, executorService);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * Pure Redis 테스트 실행
     */
    private void runPureRedisTest(TestData testData, int concurrentThreads) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        int tokensPerThread = testData.getAllTestTokens().size() / concurrentThreads;

        for (int i = 0; i < concurrentThreads; i++) {
            final int startIndex = i * tokensPerThread;
            final int endIndex = (i == concurrentThreads - 1) ? 
                testData.getAllTestTokens().size() : (i + 1) * tokensPerThread;

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = startIndex; j < endIndex; j++) {
                    pureRedisService.isBlacklisted(testData.getAllTestTokens().get(j));
                }
            }, executorService);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * 테스트용 토큰 생성
     */
    private String generateTestToken() {
        return "test_token_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 현재 메트릭 조회
     */
    @GetMapping("/metrics")
    public CurrentMetrics getCurrentMetrics() {
        return CurrentMetrics.builder()
                .bloomFilterMetrics(bloomFilterService.getMetrics())
                .pureRedisMetrics(pureRedisService.getMetrics())
                .build();
    }

    /**
     * 메트릭 초기화
     */
    @PostMapping("/reset-metrics")
    public String resetMetrics() {
        bloomFilterService.resetMetrics();
        pureRedisService.resetMetrics();
        return "✅ 모든 메트릭이 초기화되었습니다.";
    }

    // DTO 클래스들
    @lombok.Builder
    @lombok.Data
    public static class TestData {
        private List<String> blacklistedTokens;
        private List<String> allTestTokens;
    }

    @lombok.Builder
    @lombok.Data
    public static class PerformanceTestResult {
        private TestConfiguration testConfiguration;
        private BloomFilterTestResult bloomFilterResult;
        private PureRedisTestResult pureRedisResult;
        private PerformanceComparison comparison;

        @Override
        public String toString() {
            return String.format("""
                
                🎯 JWT 블랙리스트 성능 비교 테스트 결과
                ═══════════════════════════════════════════════════════════════════════
                
                📊 테스트 설정
                - 블랙리스트 토큰 수: %,d개
                - 총 테스트 토큰 수: %,d개
                - 동시 스레드 수: %d개
                - 테스트 시간: %s
                
                ⚡️ Bloom Filter + Redis 결과
                - 실행 시간: %,d ms
                - Redis 조회 감소율: %.1f%%
                - False Positive 비율: %.1f%%
                
                🐌 Pure Redis 결과  
                - 실행 시간: %,d ms
                - Redis 조회: %,d회 (100%%)
                
                🏆 성능 비교
                - 속도 개선: %.1f%%
                - Redis 조회 절약: %,d회 (%.1f%% 감소)
                
                💡 결론: Bloom Filter 도입으로 Redis 조회를 %.1f%% 감소시켜 
                       전체 성능을 %.1f%% 향상시켰습니다!
                ═══════════════════════════════════════════════════════════════════════
                """,
                testConfiguration.getBlacklistedTokenCount(),
                testConfiguration.getTotalTestTokenCount(),
                testConfiguration.getConcurrentThreads(),
                testConfiguration.getTestDateTime(),
                bloomFilterResult.getExecutionTimeMs(),
                bloomFilterResult.getMetrics().getRedisReductionRate(),
                bloomFilterResult.getMetrics().getFalsePositiveRate(),
                pureRedisResult.getExecutionTimeMs(),
                pureRedisResult.getMetrics().getRedisChecks(),
                comparison.getSpeedImprovementPercent(),
                comparison.getRedisQueryReduction(),
                comparison.getRedisQueryReductionPercent(),
                comparison.getRedisQueryReductionPercent(),
                comparison.getSpeedImprovementPercent()
            );
        }
    }

    @lombok.Builder
    @lombok.Data
    public static class TestConfiguration {
        private int blacklistedTokenCount;
        private int totalTestTokenCount;
        private int concurrentThreads;
        private LocalDateTime testDateTime;
    }

    @lombok.Builder
    @lombok.Data
    public static class BloomFilterTestResult {
        private long executionTimeMs;
        private MetricsTrackingTokenBlacklistService.PerformanceMetrics metrics;
    }

    @lombok.Builder
    @lombok.Data
    public static class PureRedisTestResult {
        private long executionTimeMs;
        private PureRedisTokenBlacklistService.PureRedisMetrics metrics;
    }

    @lombok.Builder
    @lombok.Data
    public static class PerformanceComparison {
        private double speedImprovementPercent;
        private long redisQueryReduction;
        private double redisQueryReductionPercent;
    }

    @lombok.Builder
    @lombok.Data
    public static class CurrentMetrics {
        private MetricsTrackingTokenBlacklistService.PerformanceMetrics bloomFilterMetrics;
        private PureRedisTokenBlacklistService.PureRedisMetrics pureRedisMetrics;
    }
}
