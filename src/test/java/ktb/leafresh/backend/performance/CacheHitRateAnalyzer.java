package ktb.leafresh.backend.performance;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 캐시 적중률을 정확하게 측정하고 분석하는 유틸리티 클래스
 */
@Component
public class CacheHitRateAnalyzer {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    
    public CacheHitRateAnalyzer(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Redis 서버 통계를 기반으로 캐시 적중률 측정
     */
    public CacheStatistics measureFromRedisStats() {
        try {
            Properties stats = redisTemplate.execute((RedisCallback<Properties>) connection -> 
                connection.info("stats")
            );
            
            return parseRedisStats(stats);
        } catch (Exception e) {
            System.err.println("Redis 통계 수집 실패: " + e.getMessage());
            return new CacheStatistics(0, 0, 0.0);
        }
    }
    
    /**
     * Properties 객체를 파싱하여 CacheStatistics 객체 생성
     */
    private CacheStatistics parseRedisStats(Properties statsProperties) {
        long keyspaceHits = 0;
        long keyspaceMisses = 0;
        
        try {
            String hitsStr = statsProperties.getProperty("keyspace_hits", "0");
            String missesStr = statsProperties.getProperty("keyspace_misses", "0");
            
            keyspaceHits = Long.parseLong(hitsStr);
            keyspaceMisses = Long.parseLong(missesStr);
        } catch (NumberFormatException e) {
            System.err.println("Redis 통계 파싱 실패: " + e.getMessage());
        }
        
        long total = keyspaceHits + keyspaceMisses;
        double hitRate = total > 0 ? (double) keyspaceHits / total * 100 : 0;
        
        return new CacheStatistics(keyspaceHits, keyspaceMisses, hitRate);
    }
    
    /**
     * 테스트 시작 전후의 Redis 통계 차이로 적중률 계산
     */
    public CacheStatistics calculateHitRateBetweenStats(CacheStatistics before, CacheStatistics after) {
        long hits = after.hits - before.hits;
        long misses = after.misses - before.misses;
        long total = hits + misses;
        
        double hitRate = total > 0 ? (double) hits / total * 100 : 0;
        
        return new CacheStatistics(hits, misses, hitRate);
    }
    
    /**
     * Redis 통계 문자열을 파싱하여 CacheStatistics 객체 생성 (문자열 버전 - 백업용)
     */
    private CacheStatistics parseRedisStatsFromString(String statsOutput) {
        long keyspaceHits = 0;
        long keyspaceMisses = 0;
        
        String[] lines = statsOutput.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("keyspace_hits:")) {
                keyspaceHits = Long.parseLong(line.split(":")[1]);
            } else if (line.startsWith("keyspace_misses:")) {
                keyspaceMisses = Long.parseLong(line.split(":")[1]);
            }
        }
        
        long total = keyspaceHits + keyspaceMisses;
        double hitRate = total > 0 ? (double) keyspaceHits / total * 100 : 0;
        
        return new CacheStatistics(keyspaceHits, keyspaceMisses, hitRate);
    }
    
    /**
     * 캐시 적중률 리포트 출력
     */
    public void printHitRateReport(CacheStatistics stats, int totalAppRequests, long testDurationMs) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("📊 캐시 적중률 상세 분석 리포트");
        System.out.println("=".repeat(70));
        
        System.out.printf("🔢 총 애플리케이션 요청: %,d회\n", totalAppRequests);
        System.out.printf("🎯 총 Redis 요청: %,d회\n", stats.hits + stats.misses);
        System.out.printf("✅ 캐시 적중(Hit): %,d회\n", stats.hits);
        System.out.printf("❌ 캐시 미스(Miss): %,d회\n", stats.misses);
        System.out.printf("📈 캐시 적중률: %.3f%%\n", stats.hitRate);
        System.out.printf("⏱️  테스트 소요시간: %,dms\n", testDurationMs);
        System.out.printf("⚡ 평균 TPS: %.1f requests/sec\n", 
                (double) totalAppRequests / (testDurationMs / 1000.0));
        
        System.out.println("\n" + "-".repeat(70));
        System.out.println("🎖️  포트폴리오 작성용 핵심 수치:");
        System.out.println("-".repeat(70));
        
        // 소수점 처리에 따른 다양한 표현
        if (stats.hitRate >= 99.9) {
            System.out.printf("🏆 %.3f%% 캐시 적중률 달성 (거의 완벽한 캐시 효율성)\n", stats.hitRate);
        } else if (stats.hitRate >= 99.0) {
            System.out.printf("🥇 %.2f%% 캐시 적중률 달성 (최우수 수준)\n", stats.hitRate);
        } else if (stats.hitRate >= 95.0) {
            System.out.printf("🥈 %.2f%% 캐시 적중률 달성 (우수 수준)\n", stats.hitRate);
        } else if (stats.hitRate >= 85.0) {
            System.out.printf("🥉 %.2f%% 캐시 적중률 달성 (목표 달성)\n", stats.hitRate);
        } else {
            System.out.printf("⚠️  %.2f%% 캐시 적중률 (개선 필요)\n", stats.hitRate);
        }
        
        // 비즈니스 임팩트 계산
        long effectiveRequests = stats.hits;
        double dbLoadReduction = (double) effectiveRequests / totalAppRequests * 100;
        
        System.out.printf("💾 DB 부하 감소: %.1f%% (%,d회 요청이 캐시에서 처리)\n", 
                dbLoadReduction, effectiveRequests);
        System.out.printf("🚀 성능 향상 배수: 약 %.1fx (캐시 히트 시)\n", 
                calculatePerformanceMultiplier(stats.hitRate));
        
        System.out.println("\n" + "=".repeat(70));
        
        // 추가 인사이트
        printAdditionalInsights(stats, totalAppRequests);
    }
    
    private double calculatePerformanceMultiplier(double hitRate) {
        // 가정: 캐시 히트는 13ms, DB 직접은 145ms
        double cacheTime = 13.0;
        double dbTime = 145.0;
        double averageTime = (hitRate / 100.0 * cacheTime) + ((100 - hitRate) / 100.0 * dbTime);
        
        return dbTime / averageTime;
    }
    
    private void printAdditionalInsights(CacheStatistics stats, int totalRequests) {
        System.out.println("💡 추가 인사이트:");
        
        if (stats.hitRate >= 99.0) {
            System.out.println("   • 타임딜 서비스 특성이 캐시 최적화에 완벽하게 부합");
            System.out.println("   • 반복 조회 패턴이 매우 효과적으로 작동");
            System.out.println("   • 시스템 확장성과 사용자 경험 모두 최적화 달성");
        } else if (stats.hitRate >= 95.0) {
            System.out.println("   • 우수한 캐시 전략으로 안정적인 성능 확보");
            System.out.println("   • TTL 최적화로 추가 개선 여지 존재");
        } else if (stats.hitRate >= 85.0) {
            System.out.println("   • 캐시 효과는 있으나 추가 최적화 권장");
            System.out.println("   • Cache Warming 전략 검토 필요");
        } else {
            System.out.println("   ⚠️  캐시 전략 전면 재검토 필요");
            System.out.println("   ⚠️  데이터 접근 패턴 분석 권장");
        }
        
        System.out.println("\n📝 권장 포트폴리오 문구:");
        System.out.printf("   \"Redis 캐시 도입을 통해 %.2f%% 적중률을 달성하여\"\n", stats.hitRate);
        System.out.printf("   \"시스템 응답속도를 %.1f배 개선하고 DB 부하를 %.0f%% 감소시켰습니다.\"\n", 
                calculatePerformanceMultiplier(stats.hitRate),
                (double) stats.hits / (stats.hits + stats.misses) * 100);
    }
    
    /**
     * 캐시 통계를 담는 데이터 클래스
     */
    public static class CacheStatistics {
        public final long hits;
        public final long misses;
        public final double hitRate;
        
        public CacheStatistics(long hits, long misses, double hitRate) {
            this.hits = hits;
            this.misses = misses;
            this.hitRate = hitRate;
        }
        
        @Override
        public String toString() {
            return String.format("CacheStats{hits=%d, misses=%d, hitRate=%.3f%%}", 
                    hits, misses, hitRate);
        }
    }
}
