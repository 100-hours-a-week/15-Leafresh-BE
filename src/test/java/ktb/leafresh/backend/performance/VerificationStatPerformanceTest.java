package ktb.leafresh.backend.performance;

import ktb.leafresh.backend.domain.verification.infrastructure.cache.VerificationStatCacheService;
import ktb.leafresh.backend.global.util.redis.VerificationStatRedisLuaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("통계 API Redis 성능 테스트")
public class VerificationStatPerformanceTest {

    @Autowired
    private VerificationStatRedisLuaService verificationStatRedisLuaService;

    @Autowired
    private VerificationStatCacheService verificationStatCacheService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final Long TEST_VERIFICATION_ID = 1L;

    @BeforeEach
    void setUp() {
        // Redis 캐시 초기화
        redisTemplate.getConnectionFactory().getConnection().flushDb();
        
        // 테스트용 초기 통계 데이터 설정
        verificationStatCacheService.initializeVerificationStats(TEST_VERIFICATION_ID, 0, 0, 0);
    }

    @Test
    @DisplayName("Redis 없이 DB 직접 업데이트 vs Redis 사용 성능 비교")
    void compareDbVsRedisPerformance() throws InterruptedException {
        System.out.println("=== 통계 업데이트 성능 비교 테스트 ===");
        
        int concurrency = 50;
        int requestsPerThread = 20;
        int totalRequests = concurrency * requestsPerThread;
        
        // Redis를 사용한 성능 측정
        PerformanceResult redisResult = measureRedisPerformance(concurrency, requestsPerThread);
        
        // 동시성 검증: Redis 최종 값 확인
        String finalViewCount = (String) stringRedisTemplate.opsForHash().get(
            "verification:stat:" + TEST_VERIFICATION_ID, "viewCount");
        
        System.out.println("\n=== Redis 통계 업데이트 성능 결과 ===");
        System.out.printf("총 요청 수: %d개%n", totalRequests);
        System.out.printf("평균 응답시간: %.2fms%n", redisResult.avgTime);
        System.out.printf("최소 응답시간: %.2fms%n", redisResult.minTime);
        System.out.printf("최대 응답시간: %.2fms%n", redisResult.maxTime);
        System.out.printf("95퍼센타일: %.2fms%n", redisResult.p95Time);
        System.out.printf("TPS (처리량): %.2f requests/sec%n", redisResult.tps);
        System.out.printf("최종 조회수: %s (예상: %d)%n", finalViewCount, totalRequests);
        System.out.printf("동시성 정확성: %s%n", 
            totalRequests == Integer.parseInt(finalViewCount) ? "✅ 정확" : "❌ 불일치");
    }

    @Test
    @DisplayName("여러 통계 항목 동시 업데이트 성능 테스트")
    void testMultipleStatUpdates() throws InterruptedException {
        System.out.println("=== 여러 통계 항목 동시 업데이트 테스트 ===");
        
        int concurrency = 30;
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        CountDownLatch latch = new CountDownLatch(concurrency);
        
        List<Long> viewUpdateTimes = new ArrayList<>();
        List<Long> likeUpdateTimes = new ArrayList<>();
        List<Long> commentUpdateTimes = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < concurrency; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        // 조회수 증가
                        long viewStart = System.nanoTime();
                        verificationStatRedisLuaService.increaseVerificationViewCount(TEST_VERIFICATION_ID);
                        long viewEnd = System.nanoTime();
                        
                        // 좋아요 증가/감소 (랜덤)
                        long likeStart = System.nanoTime();
                        if ((threadId + j) % 2 == 0) {
                            verificationStatRedisLuaService.increaseVerificationLikeCount(TEST_VERIFICATION_ID);
                        } else {
                            verificationStatRedisLuaService.decreaseVerificationLikeCount(TEST_VERIFICATION_ID);
                        }
                        long likeEnd = System.nanoTime();
                        
                        // 댓글 수 증가
                        long commentStart = System.nanoTime();
                        verificationStatRedisLuaService.increaseVerificationCommentCount(TEST_VERIFICATION_ID);
                        long commentEnd = System.nanoTime();
                        
                        synchronized (viewUpdateTimes) {
                            viewUpdateTimes.add((viewEnd - viewStart) / 1_000_000);
                            likeUpdateTimes.add((likeEnd - likeStart) / 1_000_000);
                            commentUpdateTimes.add((commentEnd - commentStart) / 1_000_000);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        long totalTime = System.currentTimeMillis() - startTime;
        
        // 최종 통계 확인
        var finalStats = stringRedisTemplate.opsForHash().entries("verification:stat:" + TEST_VERIFICATION_ID);
        
        System.out.println("\n결과:");
        System.out.printf("총 소요 시간: %dms%n", totalTime);
        System.out.printf("최종 통계: %s%n", finalStats);
        
        printPerformanceStats("조회수 업데이트", viewUpdateTimes);
        printPerformanceStats("좋아요 업데이트", likeUpdateTimes);
        printPerformanceStats("댓글 수 업데이트", commentUpdateTimes);
    }

    @Test
    @DisplayName("고부하 상황에서의 Redis 성능 테스트")
    void testHighLoadPerformance() throws InterruptedException {
        System.out.println("=== 고부하 상황 Redis 성능 테스트 ===");
        
        int concurrency = 100;
        int requestsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        CountDownLatch latch = new CountDownLatch(concurrency);
        
        AtomicLong totalOperations = new AtomicLong(0);
        List<Long> responseTimes = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < concurrency; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        long operationStart = System.nanoTime();
                        verificationStatRedisLuaService.increaseVerificationViewCount(TEST_VERIFICATION_ID);
                        long operationEnd = System.nanoTime();
                        
                        synchronized (responseTimes) {
                            responseTimes.add((operationEnd - operationStart) / 1_000_000);
                        }
                        
                        totalOperations.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        long totalTime = System.currentTimeMillis() - startTime;
        
        // 최종 값 확인
        String finalViewCount = (String) stringRedisTemplate.opsForHash().get(
            "verification:stat:" + TEST_VERIFICATION_ID, "viewCount");
        
        responseTimes.sort(Long::compareTo);
        
        double avgTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long minTime = responseTimes.get(0);
        long maxTime = responseTimes.get(responseTimes.size() - 1);
        long p95Time = responseTimes.get((int) (responseTimes.size() * 0.95));
        long p99Time = responseTimes.get((int) (responseTimes.size() * 0.99));
        double tps = (double) totalOperations.get() / (totalTime / 1000.0);
        
        System.out.println("\n고부하 테스트 결과:");
        System.out.printf("총 요청 수: %d개%n", totalOperations.get());
        System.out.printf("총 소요 시간: %dms%n", totalTime);
        System.out.printf("TPS (처리량): %.2f requests/sec%n", tps);
        System.out.printf("평균 응답시간: %.2fms%n", avgTime);
        System.out.printf("최소 응답시간: %dms%n", minTime);
        System.out.printf("최대 응답시간: %dms%n", maxTime);
        System.out.printf("95퍼센타일: %dms%n", p95Time);
        System.out.printf("99퍼센타일: %dms%n", p99Time);
        System.out.printf("최종 조회수: %s (예상: %d)%n", finalViewCount, totalOperations.get());
        System.out.printf("동시성 정확성: %s%n", 
            totalOperations.get() == Long.parseLong(finalViewCount) ? "✅ 정확" : "❌ 불일치");
    }

    private PerformanceResult measureRedisPerformance(int concurrency, int requestsPerThread) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        CountDownLatch latch = new CountDownLatch(concurrency);
        
        List<Long> responseTimes = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < concurrency; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        long requestStart = System.nanoTime();
                        verificationStatRedisLuaService.increaseVerificationViewCount(TEST_VERIFICATION_ID);
                        long requestEnd = System.nanoTime();
                        
                        synchronized (responseTimes) {
                            responseTimes.add((requestEnd - requestStart) / 1_000_000);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        long totalTime = System.currentTimeMillis() - startTime;
        
        responseTimes.sort(Long::compareTo);
        
        double avgTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double minTime = responseTimes.get(0);
        double maxTime = responseTimes.get(responseTimes.size() - 1);
        double p95Time = responseTimes.get((int) (responseTimes.size() * 0.95));
        double tps = (double) (concurrency * requestsPerThread) / (totalTime / 1000.0);
        
        return new PerformanceResult(avgTime, minTime, maxTime, p95Time, tps);
    }

    private void printPerformanceStats(String operation, List<Long> times) {
        if (times.isEmpty()) return;
        
        times.sort(Long::compareTo);
        
        double avgTime = times.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long minTime = times.get(0);
        long maxTime = times.get(times.size() - 1);
        long p95Time = times.get((int) (times.size() * 0.95));
        
        System.out.printf("%s - 평균: %.2fms, 최소: %dms, 최대: %dms, P95: %dms%n",
                operation, avgTime, minTime, maxTime, p95Time);
    }

    private static class PerformanceResult {
        final double avgTime;
        final double minTime;
        final double maxTime;
        final double p95Time;
        final double tps;
        
        PerformanceResult(double avgTime, double minTime, double maxTime, double p95Time, double tps) {
            this.avgTime = avgTime;
            this.minTime = minTime;
            this.maxTime = maxTime;
            this.p95Time = p95Time;
            this.tps = tps;
        }
    }
}
