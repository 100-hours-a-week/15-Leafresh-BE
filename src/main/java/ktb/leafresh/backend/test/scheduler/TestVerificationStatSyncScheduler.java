package ktb.leafresh.backend.test.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("scheduler-test") // 테스트 프로파일에서만 활성화
public class TestVerificationStatSyncScheduler {

    private final StringRedisTemplate stringRedisTemplate;
    private final String instanceId = generateInstanceId();
    
    private static String generateInstanceId() {
        try {
            return InetAddress.getLocalHost().getHostName() + "-" + UUID.randomUUID().toString().substring(0, 8);
        } catch (Exception e) {
            return "unknown-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }

    /**
     * 테스트용 스케줄러 - 30초마다 실행
     * ShedLock 적용되어 있어서 분산 환경에서 중복 실행 방지
     */
    @Scheduled(fixedDelay = 10 * 1000) // 10초마다
    @SchedulerLock(
        name = "TestVerificationStatSyncScheduler",
        lockAtLeastFor = "10s",  // 최소 10초 락 유지
        lockAtMostFor = "25s"    // 최대 25초 후 락 해제
    )
    public void testSyncVerificationStats() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        
        log.info("🔥 [인스턴스 {}] [{}] 스케줄러 실행 시작!", instanceId, timestamp);
        
        try {
            // 실제 작업 시뮬레이션 (3-7초 랜덤)
            int workTime = 3 + (int)(Math.random() * 5);
            Thread.sleep(workTime * 1000);
            
            // Redis에 실행 기록 저장
            String key = "scheduler:test:execution:" + timestamp.replace(":", "");
            stringRedisTemplate.opsForValue().set(key, instanceId);
            stringRedisTemplate.expire(key, java.time.Duration.ofMinutes(10));
            
            log.info("✅ [인스턴스 {}] [{}] 스케줄러 실행 완료! ({}초 소요)", instanceId, timestamp, workTime);
            
        } catch (InterruptedException e) {
            log.warn("❌ [인스턴스 {}] [{}] 스케줄러 실행 중 인터럽트", instanceId, timestamp);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("❌ [인스턴스 {}] [{}] 스케줄러 실행 중 오류: {}", instanceId, timestamp, e.getMessage());
        }
    }

    /**
     * ShedLock 없는 비교용 스케줄러 - 30초마다 실행
     * 분산 환경에서 중복 실행됨
     */
    @Scheduled(fixedDelay = 10 * 1000) // 10초마다
    // @SchedulerLock 주석 처리 = 분산락 없음
    public void testSyncWithoutLock() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        
        log.info("🚨 [NO-LOCK] [인스턴스 {}] [{}] 락 없는 스케줄러 실행!", instanceId, timestamp);
        
        try {
            // 짧은 작업 시뮬레이션
            Thread.sleep(2000);
            
            // Redis에 실행 기록 저장
            String key = "scheduler:no-lock:execution:" + timestamp.replace(":", "");
            stringRedisTemplate.opsForValue().set(key, instanceId);
            stringRedisTemplate.expire(key, java.time.Duration.ofMinutes(10));
            
            log.info("🚨 [NO-LOCK] [인스턴스 {}] [{}] 락 없는 스케줄러 완료!", instanceId, timestamp);
            
        } catch (InterruptedException e) {
            log.warn("❌ [NO-LOCK] [인스턴스 {}] 스케줄러 인터럽트", instanceId);
            Thread.currentThread().interrupt();
        }
    }
}
