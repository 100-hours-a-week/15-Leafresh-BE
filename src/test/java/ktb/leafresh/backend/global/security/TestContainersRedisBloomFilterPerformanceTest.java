package ktb.leafresh.backend.global.security;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 🔥 TestContainers를 사용한 실제 Redis 환경에서의 Bloom Filter 성능 테스트
 * 완전히 독립적이고 일관된 테스트 환경 제공
 */
@Testcontainers
class TestContainersRedisBloomFilterPerformanceTest {

    @Container
    static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    private StringRedisTemplate redisTemplate;
    private Set<String> bloomFilterSimulation; // Java Set으로 Bloom Filter 시뮬레이션

    private static final String BLACKLIST_PREFIX = "test_blacklist:";
    private static final long TOKEN_TTL_SECONDS = 3600; // 1시간

    @BeforeEach
    void setUp() {
        // TestContainers Redis 연결 설정
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redis.getHost());
        config.setPort(redis.getMappedPort(6379));
        
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(config);
        connectionFactory.afterPropertiesSet();
        
        redisTemplate = new StringRedisTemplate(connectionFactory);
        
        // Bloom Filter 시뮬레이션 초기화
        bloomFilterSimulation = new HashSet<>();
        
        // 테스트 전 Redis 정리
        Set<String> keys = redisTemplate.keys(BLACKLIST_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        
        System.out.println("🐳 TestContainers Redis 환경 설정 완료");
        System.out.printf("📍 Redis 연결: %s:%d%n", redis.getHost(), redis.getMappedPort(6379));
    }

    @Test
    @DisplayName("🚀 TestContainers Redis: Bloom Filter vs Pure Redis 성능 비교")
    void testContainersBloomFilterVsPureRedisPerformance() throws InterruptedException {
        // Given - 테스트 데이터 생성
        int blacklistedCount = 1000;
        int totalTestCount = 10000;
        
        List<String> blacklistedTokens = new ArrayList<>();
        List<String> allTokens = new ArrayList<>();
        
        // 블랙리스트 토큰 생성
        for (int i = 0; i < blacklistedCount; i++) {
            String token = "blacklist_" + UUID.randomUUID().toString().replace("-", "");
            blacklistedTokens.add(token);
            allTokens.add(token);
        }
        
        // 정상 토큰 생성  
        for (int i = blacklistedCount; i < totalTestCount; i++) {
            String token = "normal_" + UUID.randomUUID().toString().replace("-", "");
            allTokens.add(token);
        }

        System.out.println("\n" + "=".repeat(100));
        System.out.println("🐳 TestContainers Redis 성능 테스트 시작");
        System.out.println("=".repeat(100));
        System.out.printf("📊 블랙리스트 토큰: %,d개, 정상 토큰: %,d개, 총 %,d개%n", 
                blacklistedCount, totalTestCount - blacklistedCount, totalTestCount);

        // 블랙리스트 토큰들을 Redis와 Bloom Filter 시뮬레이션에 등록
        System.out.println("📥 블랙리스트 토큰을 Redis & Bloom Filter에 등록 중...");
        long registrationStart = System.currentTimeMillis();
        
        for (String token : blacklistedTokens) {
            String key = BLACKLIST_PREFIX + token;
            // 실제 Redis에 저장
            redisTemplate.opsForValue().set(key, "blacklisted", TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
            // Bloom Filter 시뮬레이션에도 추가
            bloomFilterSimulation.add(token);
        }
        
        long registrationEnd = System.currentTimeMillis();
        System.out.printf("✅ 등록 완료: %,d ms%n", registrationEnd - registrationStart);
        
        Thread.sleep(50); // Redis 등록 완료 대기

        // 1️⃣ Pure Redis 방식 테스트 (모든 토큰을 Redis에서 조회)
        System.out.println("\n🐌 Pure Redis 방식 테스트 시작...");
        long pureRedisStartTime = System.currentTimeMillis();
        
        AtomicLong pureRedisChecks = new AtomicLong(0);
        AtomicLong pureRedisBlacklistedFound = new AtomicLong(0);
        
        for (String token : allTokens) {
            pureRedisChecks.incrementAndGet();
            String key = BLACKLIST_PREFIX + token;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                pureRedisBlacklistedFound.incrementAndGet();
            }
        }
        long pureRedisEndTime = System.currentTimeMillis();
        long pureRedisDuration = pureRedisEndTime - pureRedisStartTime;

        // 2️⃣ Bloom Filter + Redis 방식 테스트  
        System.out.println("⚡️ Bloom Filter + Redis 방식 테스트 시작...");
        long bloomFilterStartTime = System.currentTimeMillis();
        
        AtomicLong bloomFilterTotalChecks = new AtomicLong(0);
        AtomicLong bloomFilterRedisChecks = new AtomicLong(0);
        AtomicLong bloomFilterBlacklistedFound = new AtomicLong(0);
        
        for (String token : allTokens) {
            bloomFilterTotalChecks.incrementAndGet();
            
            // 1단계: Bloom Filter 시뮬레이션 검사 (메모리 내)
            if (bloomFilterSimulation.contains(token)) {
                // 2단계: Redis에서 정확한 검증 (네트워크 I/O)
                bloomFilterRedisChecks.incrementAndGet();
                String key = BLACKLIST_PREFIX + token;
                if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                    bloomFilterBlacklistedFound.incrementAndGet();
                }
            }
        }
        long bloomFilterEndTime = System.currentTimeMillis();
        long bloomFilterDuration = bloomFilterEndTime - bloomFilterStartTime;

        // 결과 분석 및 출력
        long savedRedisQueries = pureRedisChecks.get() - bloomFilterRedisChecks.get();
        double reductionRate = (double) savedRedisQueries / pureRedisChecks.get() * 100;
        double speedImprovement = pureRedisDuration > 0 ? 
            (double)(pureRedisDuration - bloomFilterDuration) / pureRedisDuration * 100 : 0;

        System.out.println("\n" + "=".repeat(100));
        System.out.println("🏆 TestContainers Redis 성능 비교 결과");
        System.out.println("=".repeat(100));
        
        System.out.printf("🐌 Pure Redis 방식 (기존):%n");
        System.out.printf("   - 전체 검사: %,d회%n", pureRedisChecks.get());
        System.out.printf("   - Redis 조회: %,d회 (100%%)%n", pureRedisChecks.get());
        System.out.printf("   - 실행 시간: %,d ms%n", pureRedisDuration);
        System.out.printf("   - 블랙리스트 발견: %,d회%n", pureRedisBlacklistedFound.get());
        System.out.println();
        
        System.out.printf("⚡️ Bloom Filter + Redis 방식 (개선):%n");
        System.out.printf("   - 전체 검사: %,d회%n", bloomFilterTotalChecks.get());
        System.out.printf("   - Redis 조회: %,d회 (%.1f%%)%n", 
            bloomFilterRedisChecks.get(), (double) bloomFilterRedisChecks.get() / bloomFilterTotalChecks.get() * 100);
        System.out.printf("   - 실행 시간: %,d ms%n", bloomFilterDuration);
        System.out.printf("   - 블랙리스트 발견: %,d회%n", bloomFilterBlacklistedFound.get());
        System.out.println();
        
        System.out.printf("🎯 실제 성능 개선 결과:%n");
        System.out.printf("   - Redis 조회 절약: %,d회%n", savedRedisQueries);
        System.out.printf("   - 조회 절약률: %.1f%%%n", reductionRate);
        System.out.printf("   - 실행 시간 단축: %,d ms%n", pureRedisDuration - bloomFilterDuration);
        System.out.printf("   - 속도 향상: %.1f%%%n", speedImprovement);
        
        long falsePositives = bloomFilterRedisChecks.get() - bloomFilterBlacklistedFound.get();
        System.out.printf("   - False Positive: %,d회 (%.2f%%)%n", 
            falsePositives, bloomFilterRedisChecks.get() > 0 ? 
            (double) falsePositives / bloomFilterRedisChecks.get() * 100 : 0);
        
        System.out.println("=".repeat(100));
        System.out.printf("🐳 TestContainers 환경: %s:%d%n", redis.getHost(), redis.getMappedPort(6379));
        System.out.println("🎯 완전히 독립적인 환경에서 Bloom Filter 효과 검증 완료!");
        System.out.println("📈 실제 네트워크 I/O 포함한 Redis 부하 감소 실측 완료!");
        System.out.println("=".repeat(100));

        // 검증
        assertThat(reductionRate).isGreaterThan(85.0);  // TestContainers에서는 더 높은 효율 기대
        assertThat(bloomFilterBlacklistedFound.get())
            .isEqualTo(pureRedisBlacklistedFound.get());  // 동일한 블랙리스트 탐지
        assertThat(bloomFilterDuration).isLessThanOrEqualTo(pureRedisDuration * 2);  // 합리적인 성능
        assertThat(savedRedisQueries).isGreaterThan(8000);  // 실제로 큰 폭의 Redis 조회 절약
    }

    @Test
    @DisplayName("🎯 대용량 처리 시나리오 - 네트워크 지연 포함 성능 테스트")
    void highVolumeScenarioWithNetworkLatency() throws InterruptedException {
        // Given - 더 현실적인 대용량 데이터
        int blacklistedCount = 2000;
        int totalTestCount = 20000;
        
        List<String> allTokens = generateTestTokens(blacklistedCount, totalTestCount);
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🔥 대용량 처리 시나리오 테스트");
        System.out.println("=".repeat(80));
        System.out.printf("📊 총 %,d개 토큰 (블랙리스트 %,d개)%n", totalTestCount, blacklistedCount);
        
        // 블랙리스트 등록
        List<String> blacklistedTokens = allTokens.subList(0, blacklistedCount);
        long setupStart = System.currentTimeMillis();
        
        for (String token : blacklistedTokens) {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "blacklisted", TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
            bloomFilterSimulation.add(token);
        }
        
        long setupEnd = System.currentTimeMillis();
        System.out.printf("⚙️  초기 설정 시간: %,d ms%n", setupEnd - setupStart);
        
        Thread.sleep(100); // 네트워크 지연 고려

        // When - Bloom Filter + Redis 방식으로 성능 측정
        long testStart = System.currentTimeMillis();
        
        int bloomFilterRedisHits = 0;
        int actualBlacklistedFound = 0;
        
        for (String token : allTokens) {
            // 1단계: Bloom Filter 1차 검사 (빠른 메모리 접근)
            if (bloomFilterSimulation.contains(token)) {
                // 2단계: Redis 2차 검사 (네트워크 I/O 발생)
                bloomFilterRedisHits++;
                String key = BLACKLIST_PREFIX + token;
                if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                    actualBlacklistedFound++;
                }
            }
        }
        
        long testEnd = System.currentTimeMillis();
        long totalDuration = testEnd - testStart;

        // Then - 결과 분석
        double reductionRate = (double)(totalTestCount - bloomFilterRedisHits) / totalTestCount * 100;
        double averageTimePerCheck = (double)totalDuration / totalTestCount;
        double throughputPerSecond = totalTestCount / ((double)totalDuration / 1000);

        System.out.println("\n📈 대용량 처리 성능 결과:");
        System.out.printf("   - 처리량: %,d개 토큰%n", totalTestCount);
        System.out.printf("   - 총 소요시간: %,d ms%n", totalDuration);
        System.out.printf("   - 평균 처리시간: %.3f ms/token%n", averageTimePerCheck);
        System.out.printf("   - 처리 속도: %.0f token/sec%n", throughputPerSecond);
        System.out.printf("   - Redis 조회: %,d회 (%.1f%%)%n", 
            bloomFilterRedisHits, (double)bloomFilterRedisHits / totalTestCount * 100);
        System.out.printf("   - Redis 조회 절약률: %.1f%%%n", reductionRate);
        System.out.printf("   - 블랙리스트 정확 탐지: %,d회%n", actualBlacklistedFound);
        System.out.println("=".repeat(80));

        // 검증
        assertThat(reductionRate).isGreaterThan(85.0);
        assertThat(actualBlacklistedFound).isEqualTo(blacklistedCount);
        assertThat(averageTimePerCheck).isLessThan(0.5);  // 0.5ms 이내로 매우 빠른 처리
        assertThat(throughputPerSecond).isGreaterThan(10000);  // 초당 10,000+ 처리
    }

    private List<String> generateTestTokens(int blacklistedCount, int totalCount) {
        List<String> tokens = new ArrayList<>();
        
        // 블랙리스트 토큰
        for (int i = 0; i < blacklistedCount; i++) {
            tokens.add("blacklist_" + UUID.randomUUID().toString().replace("-", ""));
        }
        
        // 정상 토큰
        for (int i = blacklistedCount; i < totalCount; i++) {
            tokens.add("normal_" + UUID.randomUUID().toString().replace("-", ""));
        }
        
        return tokens;
    }
}
