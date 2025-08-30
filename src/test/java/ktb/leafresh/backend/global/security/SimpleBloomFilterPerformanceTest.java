package ktb.leafresh.backend.global.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 간단한 Bloom Filter 성능 데모
 * 실제 Redis 없이 Bloom Filter 원리를 시연
 */
class SimpleBloomFilterPerformanceTest {

    @Test
    @DisplayName("Bloom Filter 원리 시연 - Redis 조회 절약 효과 확인")
    void demonstrateBloomFilterPrinciple() {
        // Given - 테스트 데이터
        int blacklistedCount = 1000;
        int totalTestCount = 10000;
        
        // 실제 블랙리스트 (Redis에 저장된다고 가정)
        Set<String> actualBlacklist = new HashSet<>(); 
        
        // 간단한 Bloom Filter 시뮬레이션 (실제로는 비트 배열 사용)
        Set<String> bloomFilter = new HashSet<>();
        
        // 메트릭 추적
        AtomicLong bloomFilterChecks = new AtomicLong(0);
        AtomicLong redisChecks = new AtomicLong(0);  // Redis 조회 횟수
        AtomicLong actualBlacklistedFound = new AtomicLong(0);

        // 블랙리스트 토큰 생성
        List<String> blacklistedTokens = new ArrayList<>();
        for (int i = 0; i < blacklistedCount; i++) {
            String token = "blacklisted_" + UUID.randomUUID().toString().replace("-", "");
            blacklistedTokens.add(token);
            actualBlacklist.add(token);
            bloomFilter.add(token); // Bloom Filter에도 추가
        }

        // 전체 테스트 토큰 생성 (블랙리스트 + 정상 토큰)
        List<String> allTestTokens = new ArrayList<>(blacklistedTokens);
        for (int i = blacklistedCount; i < totalTestCount; i++) {
            String token = "normal_" + UUID.randomUUID().toString().replace("-", "");
            allTestTokens.add(token);
        }

        // When - Bloom Filter + Redis 방식으로 검사
        for (String token : allTestTokens) {
            bloomFilterChecks.incrementAndGet();
            
            // 1단계: Bloom Filter 검사
            if (bloomFilter.contains(token)) {
                // Bloom Filter에서 "존재할 수 있음"으로 판단
                // 2단계: Redis에서 정확한 검증
                redisChecks.incrementAndGet();
                
                if (actualBlacklist.contains(token)) {
                    actualBlacklistedFound.incrementAndGet();
                }
                // False Positive의 경우 Redis 조회는 했지만 실제로는 블랙리스트가 아님
            }
            // Bloom Filter에서 "확실히 없음"으로 판단 시 Redis 조회 생략
        }

        // Then - 성능 분석
        long totalChecks = bloomFilterChecks.get();
        long savedRedisQueries = totalChecks - redisChecks.get();
        double reductionRate = (double) savedRedisQueries / totalChecks * 100;

        System.out.println("\n" + "=".repeat(80));
        System.out.println("🎯 Bloom Filter 성능 데모 결과");
        System.out.println("=".repeat(80));
        System.out.printf("📊 전체 토큰 검사: %,d회%n", totalChecks);
        System.out.printf("🔍 Redis 조회 필요: %,d회 (%.1f%%)%n", 
            redisChecks.get(), (double) redisChecks.get() / totalChecks * 100);
        System.out.printf("⚡️ Redis 조회 절약: %,d회 (%.1f%%)%n", 
            savedRedisQueries, reductionRate);
        System.out.printf("✅ 실제 블랙리스트 발견: %,d회%n", actualBlacklistedFound.get());
        System.out.printf("🎯 정확도: %.1f%% (False Negative 없음 보장)%n", 
            actualBlacklistedFound.get() == blacklistedCount ? 100.0 : 0.0);
        
        // False Positive 계산
        long falsePositives = redisChecks.get() - actualBlacklistedFound.get();
        System.out.printf("📈 False Positive: %,d회 (%.1f%%)%n", 
            falsePositives, redisChecks.get() > 0 ? (double) falsePositives / redisChecks.get() * 100 : 0);
        
        System.out.println("=".repeat(80));
        System.out.println("💡 핵심 원리:");
        System.out.println("   ✅ Bloom Filter가 '없음'이라고 하면 100% 신뢰 가능 → Redis 조회 생략");
        System.out.println("   ⚠️  Bloom Filter가 '있을 수 있음'이라고 하면 → Redis에서 정확한 검증 필요");
        System.out.println("   🚀 결과: 대부분의 정상 토큰은 Redis 조회 없이 빠르게 처리!");
        System.out.println("=".repeat(80));

        // 검증
        assertThat(reductionRate).isGreaterThan(70.0);  // 최소 70% 이상 Redis 조회 절약
        assertThat(actualBlacklistedFound.get()).isEqualTo(blacklistedCount);  // 모든 블랙리스트 토큰 정확히 탐지
        assertThat(savedRedisQueries).isGreaterThan(0);  // 실제로 Redis 조회 절약
    }

    @Test
    @DisplayName("Pure Redis vs Bloom Filter 성능 비교")
    void comparePerformance() {
        int blacklistedCount = 500;
        int totalTestCount = 5000;
        
        // 테스트 데이터 준비
        Set<String> actualBlacklist = new HashSet<>();
        Set<String> bloomFilter = new HashSet<>();
        
        List<String> allTokens = new ArrayList<>();
        
        // 블랙리스트 토큰 생성
        for (int i = 0; i < blacklistedCount; i++) {
            String token = "black_" + i + "_" + UUID.randomUUID().toString().substring(0, 8);
            actualBlacklist.add(token);
            bloomFilter.add(token);
            allTokens.add(token);
        }
        
        // 정상 토큰 생성
        for (int i = blacklistedCount; i < totalTestCount; i++) {
            String token = "normal_" + i + "_" + UUID.randomUUID().toString().substring(0, 8);
            allTokens.add(token);
        }

        // Pure Redis 방식 시뮬레이션
        long pureRedisStart = System.currentTimeMillis();
        int pureRedisChecks = 0;
        int pureRedisFound = 0;
        
        for (String token : allTokens) {
            pureRedisChecks++;
            if (actualBlacklist.contains(token)) {
                pureRedisFound++;
            }
        }
        long pureRedisDuration = System.currentTimeMillis() - pureRedisStart;

        // Bloom Filter + Redis 방식 시뮬레이션
        long bloomFilterStart = System.currentTimeMillis();
        int bloomChecks = 0;
        int redisChecks = 0;
        int bloomFound = 0;
        
        for (String token : allTokens) {
            bloomChecks++;
            if (bloomFilter.contains(token)) {
                redisChecks++;
                if (actualBlacklist.contains(token)) {
                    bloomFound++;
                }
            }
        }
        long bloomFilterDuration = System.currentTimeMillis() - bloomFilterStart;

        // 결과 출력
        System.out.println("\n" + "=".repeat(100));
        System.out.println("🏁 성능 비교 결과");
        System.out.println("=".repeat(100));
        
        System.out.printf("🐌 Pure Redis 방식:%n");
        System.out.printf("   - 전체 검사: %,d회%n", pureRedisChecks);
        System.out.printf("   - Redis 조회: %,d회 (100%%)%n", pureRedisChecks);
        System.out.printf("   - 실행 시간: %,d ms%n", pureRedisDuration);
        System.out.printf("   - 블랙리스트 발견: %,d회%n", pureRedisFound);
        System.out.println();
        
        System.out.printf("⚡️ Bloom Filter + Redis 방식:%n");
        System.out.printf("   - 전체 검사: %,d회%n", bloomChecks);
        System.out.printf("   - Redis 조회: %,d회 (%.1f%%)%n", 
            redisChecks, (double) redisChecks / bloomChecks * 100);
        System.out.printf("   - 실행 시간: %,d ms%n", bloomFilterDuration);
        System.out.printf("   - 블랙리스트 발견: %,d회%n", bloomFound);
        System.out.println();
        
        long savedQueries = pureRedisChecks - redisChecks;
        double reductionRate = (double) savedQueries / pureRedisChecks * 100;
        
        System.out.printf("🏆 성능 개선:%n");
        System.out.printf("   - Redis 조회 절약: %,d회%n", savedQueries);
        System.out.printf("   - 절약률: %.1f%%%n", reductionRate);
        
        if (pureRedisDuration > 0) {
            double speedImprovement = (double)(pureRedisDuration - bloomFilterDuration) / pureRedisDuration * 100;
            System.out.printf("   - 속도 향상: %.1f%%%n", speedImprovement);
        }
        
        System.out.println("=".repeat(100));

        // 검증
        assertThat(reductionRate).isGreaterThan(50.0);
        assertThat(pureRedisFound).isEqualTo(bloomFound);
        assertThat(savedQueries).isGreaterThan(0);
    }
}
