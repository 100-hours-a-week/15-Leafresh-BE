package ktb.leafresh.backend.test.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/test/scheduler")
@RequiredArgsConstructor
@Profile({"local", "scheduler-test", "dev"}) // 여러 프로파일에서 활성화
public class SchedulerTestController {

    private final StringRedisTemplate stringRedisTemplate;

    @GetMapping("/results")
    public ResponseEntity<Map<String, Object>> getSchedulerResults() {
        Map<String, Object> result = new HashMap<>();
        
        // ShedLock 적용된 스케줄러 결과
        Set<String> lockKeys = stringRedisTemplate.keys("scheduler:test:execution:*");
        Map<String, Object> lockResults = new HashMap<>();
        
        if (lockKeys != null && !lockKeys.isEmpty()) {
            Map<String, String> lockExecutions = lockKeys.stream()
                .collect(Collectors.toMap(
                    key -> key.replace("scheduler:test:execution:", ""),
                    key -> stringRedisTemplate.opsForValue().get(key)
                ));
            
            lockResults.put("total_executions", lockExecutions.size());
            lockResults.put("unique_instances", new HashSet<>(lockExecutions.values()).size());
            lockResults.put("executions", lockExecutions);
        } else {
            lockResults.put("total_executions", 0);
            lockResults.put("unique_instances", 0);
            lockResults.put("executions", Collections.emptyMap());
        }
        
        // ShedLock 없는 스케줄러 결과
        Set<String> noLockKeys = stringRedisTemplate.keys("scheduler:no-lock:execution:*");
        Map<String, Object> noLockResults = new HashMap<>();
        
        if (noLockKeys != null && !noLockKeys.isEmpty()) {
            Map<String, String> noLockExecutions = noLockKeys.stream()
                .collect(Collectors.toMap(
                    key -> key.replace("scheduler:no-lock:execution:", ""),
                    key -> stringRedisTemplate.opsForValue().get(key)
                ));
            
            noLockResults.put("total_executions", noLockExecutions.size());
            noLockResults.put("unique_instances", new HashSet<>(noLockExecutions.values()).size());
            noLockResults.put("executions", noLockExecutions);
        } else {
            noLockResults.put("total_executions", 0);
            noLockResults.put("unique_instances", 0);
            noLockResults.put("executions", Collections.emptyMap());
        }
        
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        result.put("with_shedlock", lockResults);
        result.put("without_shedlock", noLockResults);
        
        // 비교 분석
        Map<String, String> analysis = new HashMap<>();
        int lockExec = (Integer) lockResults.get("total_executions");
        int noLockExec = (Integer) noLockResults.get("total_executions");
        int lockInstances = (Integer) lockResults.get("unique_instances");
        int noLockInstances = (Integer) noLockResults.get("unique_instances");
        
        analysis.put("execution_ratio", String.format("ShedLock: %d회 vs No-Lock: %d회", lockExec, noLockExec));
        analysis.put("instance_ratio", String.format("ShedLock: %d개 인스턴스 vs No-Lock: %d개 인스턴스", lockInstances, noLockInstances));
        analysis.put("conclusion", lockExec < noLockExec ? "ShedLock이 중복 실행을 방지하고 있습니다!" : "아직 차이가 명확하지 않습니다. 더 기다려보세요.");
        
        result.put("analysis", analysis);
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/clear")
    public ResponseEntity<String> clearTestData() {
        Set<String> testKeys = stringRedisTemplate.keys("scheduler:*:execution:*");
        if (testKeys != null && !testKeys.isEmpty()) {
            stringRedisTemplate.delete(testKeys);
            return ResponseEntity.ok("테스트 데이터 " + testKeys.size() + "개를 삭제했습니다.");
        }
        return ResponseEntity.ok("삭제할 테스트 데이터가 없습니다.");
    }
}
