package ktb.leafresh.backend.global.security;

import io.rebloom.client.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JWT 블랙리스트 성능 테스트
 * Bloom Filter vs Pure Redis 성능 비교 검증
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "redis.bloom.host=localhost",
    "redis.bloom.port=6379",
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379"
})
class TokenBlacklistPerformanceTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis/redis-stack:latest")
        .withExposedPorts(6379)
        .withCommand("redis-stack-server", "--bind", "0.0.0.0");

    @Autowired
    private StringRedisTemplate redisTemplate;

    private MetricsTrackingTokenBlacklistService bloomFilterService;
    private PureRedisTokenBlacklistService pureRedisService;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @BeforeEach
    void setUp() {
        // Redis 연결 설정 업데이트
        System.setProperty("redis.bloom.host", redis.getHost());
        System.setProperty("redis.bloom.port", redis.getMappedPort(6379).toString());
        
        // Client 인스턴스 생성
        Client bloomClient = new Client(redis.getHost(), redis.getMappedPort(6379));
        
        // 서비스 인스턴스 생성
        bloomFilterService = new MetricsTrackingTokenBlacklistService(redisTemplate, bloomClient);
        pureRedisService = new PureRedisTokenBlacklistService(redisTemplate);
        
        // Redis 초기화
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        
        // 메트릭 초기화
        bloomFilterService.resetMetrics();
        pureRedisService.resetMetrics();
        
        // Bloom Filter 초기화
        try {
            bloomClient.delete("accessTokenBlacklist");
        } catch (Exception e) {
            // Bloom Filter가 존재하지 않으면 무시
        }
        
        // 새 Bloom Filter 생성 (에러율 0.01%, 예상 아이템 수 100,000)
        try {
            bloomClient.createFilter("accessTokenBlacklist", 100000, 0.0001);
        } catch (Exception e) {
            // 이미 존재하면 무시
        }
    }

    @Test
    @DisplayName("소규모 테스트 - Bloom Filter가 Redis 조회를 효과적으로 감소시키는지 확인")
    void testSmallScalePerformance() {
        // Given
        int blacklistedCount = 100;
        int totalTestCount = 1000;
        
        List<String> testTokens = prepareTestData(blacklistedCount, totalTestCount);

        // When - Bloom Filter 테스트
        long bloomFilterStart = System.currentTimeMillis();
        testTokens.forEach(bloomFilterService::isBlacklisted);
        long bloomFilterDuration = System.currentTimeMillis() - bloomFilterStart;
        
        // When - Pure Redis 테스트
        long pureRedisStart = System.currentTimeMillis();
        testTokens.forEach(pureRedisService::isBlacklisted);
        long pureRedisDuration = System.currentTimeMillis() - pureRedisStart;

        // Then
        var bloomMetrics = bloomFilterService.getMetrics();
        var redisMetrics = pureRedisService.getMetrics();

        // 결과 출력
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🎯 소규모 성능 테스트 결과");
        System.out.println("=".repeat(80));
        System.out.println(bloomMetrics);
        System.out.println(redisMetrics);
        System.out.printf("⚡️ Bloom Filter 실행시간: %d ms%n", bloomFilterDuration);
        System.out.printf("🐌 Pure Redis 실행시간: %d ms%n", pureRedisDuration);
        
        if (pureRedisDuration > 0) {
            double improvement = (double)(pureRedisDuration - bloomFilterDuration) / pureRedisDuration * 100;
            System.out.printf("🏆 성능 개선: %.1f%%%n", improvement);
        }
        System.out.println("=".repeat(80));

        // 검증
        assertThat(bloomMetrics.getTotalChecks()).isEqualTo(totalTestCount);
        assertThat(redisMetrics.getTotalChecks()).isEqualTo(totalTestCount);
        assertThat(bloomMetrics.getRedisChecks()).isLessThan(redisMetrics.getRedisChecks());
        assertThat(bloomMetrics.getRedisReductionRate()).isGreaterThan(0);
        
        // Bloom Filter가 정확한 결과를 반환하는지 확인
        assertThat(bloomMetrics.getActualBlacklistedTokens()).isEqualTo(blacklistedCount);
        assertThat(redisMetrics.getActualBlacklistedTokens()).isEqualTo(blacklistedCount);
    }

    @Test
    @DisplayName("대규모 동시성 테스트 - 높은 부하에서 Bloom Filter 효과 측정")
    void testLargeScaleConcurrentPerformance() throws Exception {
        // Given
        int blacklistedCount = 1000;
        int totalTestCount = 10000;
        int threadCount = 10;
        
        List<String> testTokens = prepareTestData(blacklistedCount, totalTestCount);

        // When - Bloom Filter 동시성 테스트
        long bloomFilterStart = System.currentTimeMillis();
        List<CompletableFuture<Void>> bloomFutures = runConcurrentTest(testTokens, threadCount, bloomFilterService::isBlacklisted);
        CompletableFuture.allOf(bloomFutures.toArray(new CompletableFuture[0])).join();
        long bloomFilterDuration = System.currentTimeMillis() - bloomFilterStart;

        // When - Pure Redis 동시성 테스트  
        long pureRedisStart = System.currentTimeMillis();
        List<CompletableFuture<Void>> redisFutures = runConcurrentTest(testTokens, threadCount, pureRedisService::isBlacklisted);
        CompletableFuture.allOf(redisFutures.toArray(new CompletableFuture[0])).join();
        long pureRedisDuration = System.currentTimeMillis() - pureRedisStart;

        // Then
        var bloomMetrics = bloomFilterService.getMetrics();
        var redisMetrics = pureRedisService.getMetrics();

        // 결과 출력
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🚀 대규모 동시성 성능 테스트 결과");
        System.out.println("=".repeat(80));
        System.out.println(bloomMetrics);
        System.out.println(redisMetrics);
        System.out.printf("⚡️ Bloom Filter 동시성 실행시간: %d ms%n", bloomFilterDuration);
        System.out.printf("🐌 Pure Redis 동시성 실행시간: %d ms%n", pureRedisDuration);
        
        if (pureRedisDuration > 0) {
            double improvement = (double)(pureRedisDuration - bloomFilterDuration) / pureRedisDuration * 100;
            System.out.printf("🏆 동시성 성능 개선: %.1f%%%n", improvement);
        }
        
        // Redis 조회 절약량 계산
        long savedQueries = bloomMetrics.getBloomFilterMisses();
        System.out.printf("💰 절약된 Redis 조회: %,d회%n", savedQueries);
        System.out.printf("📊 전체 대비 절약률: %.1f%%%n", bloomMetrics.getRedisReductionRate());
        System.out.println("=".repeat(80));

        // 검증
        assertThat(bloomMetrics.getTotalChecks()).isEqualTo(totalTestCount);
        assertThat(redisMetrics.getTotalChecks()).isEqualTo(totalTestCount);
        
        // Bloom Filter가 최소 70% 이상의 Redis 조회를 절약해야 함
        assertThat(bloomMetrics.getRedisReductionRate()).isGreaterThan(70.0);
        
        // 동시성 환경에서도 정확한 결과 반환
        assertThat(bloomMetrics.getActualBlacklistedTokens()).isEqualTo(blacklistedCount);
        assertThat(redisMetrics.getActualBlacklistedTokens()).isEqualTo(blacklistedCount);
    }

    @Test
    @DisplayName("Redis 조회 절약 비율 정확성 테스트")
    void testRedisQueryReductionAccuracy() {
        // Given - 블랙리스트가 적고 대부분이 정상 토큰인 현실적인 시나리오
        int blacklistedCount = 50;  // 실제 블랙리스트는 적음
        int totalTestCount = 2000;  // 대부분이 정상 토큰
        
        List<String> testTokens = prepareTestData(blacklistedCount, totalTestCount);

        // When - Bloom Filter로 테스트
        testTokens.forEach(bloomFilterService::isBlacklisted);

        // Then
        var metrics = bloomFilterService.getMetrics();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📊 Redis 조회 절약 정확성 테스트");
        System.out.println("=".repeat(60));
        System.out.println(metrics);
        System.out.println("=".repeat(60));

        // 검증: 정상 토큰들은 Bloom Filter에서 Miss되어 Redis 조회 생략
        long expectedNormalTokens = totalTestCount - blacklistedCount;  // 1950개의 정상 토큰
        
        // Bloom Filter Miss는 대부분 정상 토큰이어야 함 (False Negative는 없음)
        assertThat(metrics.getBloomFilterMisses()).isGreaterThan((long)(expectedNormalTokens * 0.9)); // 최소 90%
        
        // Redis 조회 감소율이 목표치 이상인지 확인
        assertThat(metrics.getRedisReductionRate()).isGreaterThan(70.0);
        
        // 모든 블랙리스트 토큰이 정확히 감지되었는지 확인
        assertThat(metrics.getActualBlacklistedTokens()).isEqualTo(blacklistedCount);
    }

    private List<String> prepareTestData(int blacklistedCount, int totalCount) {
        List<String> allTokens = new ArrayList<>();
        
        // 블랙리스트 토큰 생성 및 등록
        for (int i = 0; i < blacklistedCount; i++) {
            String token = "blacklisted_token_" + UUID.randomUUID().toString().replace("-", "");
            allTokens.add(token);
            
            // 두 서비스 모두에 등록
            long expirationTime = 3600000L; // 1시간
            bloomFilterService.blacklistAccessToken(token, expirationTime);
            pureRedisService.blacklistAccessToken(token, expirationTime);
        }
        
        // 정상 토큰들 생성 (블랙리스트에 없음)
        for (int i = blacklistedCount; i < totalCount; i++) {
            String token = "normal_token_" + UUID.randomUUID().toString().replace("-", "");
            allTokens.add(token);
        }
        
        return allTokens;
    }

    private List<CompletableFuture<Void>> runConcurrentTest(
        List<String> tokens, 
        int threadCount,
        java.util.function.Consumer<String> testFunction) {
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        int tokensPerThread = tokens.size() / threadCount;

        for (int i = 0; i < threadCount; i++) {
            final int startIndex = i * tokensPerThread;
            final int endIndex = (i == threadCount - 1) ? tokens.size() : (i + 1) * tokensPerThread;

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = startIndex; j < endIndex; j++) {
                    testFunction.accept(tokens.get(j));
                }
            }, executorService);

            futures.add(future);
        }

        return futures;
    }
}
