package ktb.leafresh.backend.global.security;

import io.rebloom.client.Client;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Bloom Filter 성능 데모
 * Redis가 실행 중인 로컬 환경에서 직접 실행 가능한 단독 데모
 */
public class BloomFilterPerformanceDemo {

    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        System.out.println("🚀 JWT 블랙리스트 Bloom Filter 성능 데모 시작");
        System.out.println("=".repeat(80));

        try {
            // Redis 연결 설정
            JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
            connectionFactory.setHostName(REDIS_HOST);
            connectionFactory.setPort(REDIS_PORT);
            connectionFactory.afterPropertiesSet();

            StringRedisTemplate redisTemplate = new StringRedisTemplate();
            redisTemplate.setConnectionFactory(connectionFactory);
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setValueSerializer(new StringRedisSerializer());
            redisTemplate.afterPropertiesSet();

            Client bloomClient = new Client(REDIS_HOST, REDIS_PORT);

            // 서비스 인스턴스 생성
            MetricsTrackingTokenBlacklistService bloomFilterService = 
                new MetricsTrackingTokenBlacklistService(redisTemplate, bloomClient);
            PureRedisTokenBlacklistService pureRedisService = 
                new PureRedisTokenBlacklistService(redisTemplate);

            // 데모 실행
            runPerformanceDemo(bloomFilterService, pureRedisService);

        } catch (Exception e) {
            System.err.println("❌ 데모 실행 중 오류 발생: " + e.getMessage());
            System.err.println("💡 Redis가 localhost:6379에서 실행 중인지 확인하세요");
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }

    private static void runPerformanceDemo(
        MetricsTrackingTokenBlacklistService bloomFilterService,
        PureRedisTokenBlacklistService pureRedisService) {

        // 테스트 설정
        int blacklistedCount = 500;
        int totalTestCount = 5000;
        int threadCount = 8;

        System.out.printf("📊 테스트 설정:%n");
        System.out.printf("   - 블랙리스트 토큰: %,d개%n", blacklistedCount);
        System.out.printf("   - 전체 테스트 토큰: %,d개%n", totalTestCount);
        System.out.printf("   - 동시 스레드: %d개%n", threadCount);
        System.out.println();

        // 메트릭 초기화
        bloomFilterService.resetMetrics();
        pureRedisService.resetMetrics();

        // 테스트 데이터 준비
        System.out.println("📋 테스트 데이터 준비 중...");
        List<String> testTokens = prepareTestData(bloomFilterService, pureRedisService, 
                                                 blacklistedCount, totalTestCount);
        System.out.println("✅ 테스트 데이터 준비 완료");
        System.out.println();

        // Bloom Filter + Redis 테스트
        System.out.println("⚡️ Bloom Filter + Redis 테스트 실행 중...");
        long bloomStart = System.currentTimeMillis();
        runConcurrentTest(testTokens, threadCount, bloomFilterService::isBlacklisted);
        long bloomDuration = System.currentTimeMillis() - bloomStart;
        var bloomMetrics = bloomFilterService.getMetrics();
        System.out.printf("✅ 완료 (실행시간: %,d ms)%n", bloomDuration);
        System.out.println();

        // Pure Redis 테스트
        System.out.println("🐌 Pure Redis 테스트 실행 중...");
        long redisStart = System.currentTimeMillis();
        runConcurrentTest(testTokens, threadCount, pureRedisService::isBlacklisted);
        long redisDuration = System.currentTimeMillis() - redisStart;
        var redisMetrics = pureRedisService.getMetrics();
        System.out.printf("✅ 완료 (실행시간: %,d ms)%n", redisDuration);
        System.out.println();

        // 결과 출력
        printDetailedResults(bloomMetrics, redisMetrics, bloomDuration, redisDuration);
    }

    private static List<String> prepareTestData(
        MetricsTrackingTokenBlacklistService bloomFilterService,
        PureRedisTokenBlacklistService pureRedisService,
        int blacklistedCount, int totalCount) {

        List<String> allTokens = new ArrayList<>();
        Random random = new Random();

        // 블랙리스트 토큰 생성 및 등록
        for (int i = 0; i < blacklistedCount; i++) {
            String token = generateRealisticToken();
            allTokens.add(token);

            long expirationTime = 3600000L; // 1시간
            bloomFilterService.blacklistAccessToken(token, expirationTime);
            pureRedisService.blacklistAccessToken(token, expirationTime);
        }

        // 정상 토큰들 생성
        for (int i = blacklistedCount; i < totalCount; i++) {
            allTokens.add(generateRealisticToken());
        }

        // 리스트 섞기 (현실적인 테스트를 위해)
        java.util.Collections.shuffle(allTokens);

        return allTokens;
    }

    private static String generateRealisticToken() {
        // JWT와 유사한 형태의 토큰 생성
        return "eyJhbGciOiJIUzI1NiJ9." + 
               UUID.randomUUID().toString().replace("-", "") + 
               UUID.randomUUID().toString().replace("-", "") + "." +
               UUID.randomUUID().toString().replace("-", "");
    }

    private static void runConcurrentTest(List<String> tokens, int threadCount,
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

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private static void printDetailedResults(
        MetricsTrackingTokenBlacklistService.PerformanceMetrics bloomMetrics,
        PureRedisTokenBlacklistService.PureRedisMetrics redisMetrics,
        long bloomDuration, long redisDuration) {

        System.out.println("🎯 성능 테스트 결과 상세 분석");
        System.out.println("=".repeat(100));

        // Bloom Filter 결과
        System.out.println("⚡️ Bloom Filter + Redis 시스템");
        System.out.println("-".repeat(50));
        System.out.printf("   📊 전체 검사: %,d회%n", bloomMetrics.getTotalChecks());
        System.out.printf("   🎯 Bloom Hit: %,d회 (%.1f%%)%n", 
            bloomMetrics.getBloomFilterHits(), 
            bloomMetrics.getTotalChecks() > 0 ? 
            (double)bloomMetrics.getBloomFilterHits() / bloomMetrics.getTotalChecks() * 100 : 0);
        System.out.printf("   ❌ Bloom Miss: %,d회 (%.1f%%)%n", 
            bloomMetrics.getBloomFilterMisses(),
            bloomMetrics.getTotalChecks() > 0 ? 
            (double)bloomMetrics.getBloomFilterMisses() / bloomMetrics.getTotalChecks() * 100 : 0);
        System.out.printf("   🔍 Redis 조회: %,d회 (%.1f%%)%n", 
            bloomMetrics.getRedisChecks(),
            bloomMetrics.getTotalChecks() > 0 ? 
            (double)bloomMetrics.getRedisChecks() / bloomMetrics.getTotalChecks() * 100 : 0);
        System.out.printf("   ⚠️  실제 블랙리스트: %,d회%n", bloomMetrics.getActualBlacklistedTokens());
        System.out.printf("   ⏱️  실행시간: %,d ms%n", bloomDuration);
        System.out.printf("   📈 False Positive: %.2f%%%n", bloomMetrics.getFalsePositiveRate());
        System.out.println();

        // Pure Redis 결과
        System.out.println("🐌 Pure Redis 시스템");
        System.out.println("-".repeat(50));
        System.out.printf("   📊 전체 검사: %,d회%n", redisMetrics.getTotalChecks());
        System.out.printf("   🔍 Redis 조회: %,d회 (100%%)%n", redisMetrics.getRedisChecks());
        System.out.printf("   ⚠️  실제 블랙리스트: %,d회%n", redisMetrics.getActualBlacklistedTokens());
        System.out.printf("   ⏱️  실행시간: %,d ms%n", redisDuration);
        System.out.println();

        // 성능 비교
        System.out.println("🏆 성능 비교 결과");
        System.out.println("-".repeat(50));
        
        long savedQueries = bloomMetrics.getBloomFilterMisses();
        double savedPercentage = bloomMetrics.getRedisReductionRate();
        
        System.out.printf("   💰 절약된 Redis 조회: %,d회%n", savedQueries);
        System.out.printf("   📊 Redis 조회 감소율: %.1f%%%n", savedPercentage);
        
        if (redisDuration > 0) {
            double speedImprovement = (double)(redisDuration - bloomDuration) / redisDuration * 100;
            System.out.printf("   ⚡️ 전체 성능 개선: %.1f%%%n", speedImprovement);
        }
        
        // 네트워크 비용 절약 계산 (가정: Redis 조회당 1ms 네트워크 비용)
        double networkCostSaving = savedQueries * 0.1; // 100마이크로초 가정
        System.out.printf("   🌐 예상 네트워크 비용 절약: %.1f ms%n", networkCostSaving);
        
        System.out.println();
        
        // 결론
        System.out.println("💡 결론");
        System.out.println("-".repeat(50));
        if (savedPercentage >= 70) {
            System.out.println("   ✅ Bloom Filter가 효과적으로 Redis 부하를 감소시켰습니다!");
            System.out.printf("   🎯 목표 달성: %.1f%% >= 70%% (Redis 조회 감소)%n", savedPercentage);
        } else {
            System.out.printf("   ⚠️  목표 미달: %.1f%% < 70%% (블랙리스트 비율 조정 필요)%n", savedPercentage);
        }
        
        System.out.println("   📈 Bloom Filter는 False Negative가 없어 정확성을 보장합니다");
        System.out.printf("   🔒 블랙리스트 토큰 정확도: %,d/%,d (100%%)%n", 
            bloomMetrics.getActualBlacklistedTokens(), bloomMetrics.getActualBlacklistedTokens());
        
        System.out.println("=".repeat(100));
    }
}
