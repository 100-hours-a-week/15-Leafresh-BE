# ShedLock 동작 원리 상세 분석

## 🔍 `@SchedulerLock` 어노테이션의 내부 동작

### 1. 락 획득 과정

```
인스턴스 A, B, C가 동시에 스케줄러 실행 시도
    ↓
Redis에 원자적 SET 연산으로 락 키 생성 시도
    ↓
SET shedlock:VerificationStatSyncScheduler "lock_info" NX EX 600
    ↓
성공한 인스턴스 1개만 락 획득, 나머지는 실패
```

### 2. Redis 명령어 레벨에서의 동작

**락 획득 시도:**
```redis
# 원자적 SET 명령어 (NX = Not eXists, EX = EXpire)
SET shedlock:VerificationStatSyncScheduler '{"lockUntil":1672531800000,"lockedAt":1672531200000,"lockedBy":"seoul-8001"}' NX EX 600

# 응답:
# - OK: 락 획득 성공 (최초 1개 인스턴스만)
# - (nil): 락 이미 존재, 획득 실패 (나머지 인스턴스들)
```

**락 상태 확인:**
```redis
GET shedlock:VerificationStatSyncScheduler
# {"lockUntil":1672531800000,"lockedAt":1672531200000,"lockedBy":"seoul-8001"}
```

**락 해제:**
```redis
DEL shedlock:VerificationStatSyncScheduler
# 또는 TTL 만료로 자동 해제
```

### 3. 시간 설정의 의미

```java
@SchedulerLock(
    name = "VerificationStatSyncScheduler",
    lockAtLeastFor = "1m",    // 최소 1분간 락 유지
    lockAtMostFor = "10m"     // 최대 10분 후 강제 해제
)
```

**lockAtLeastFor (최소 락 시간):**
- 작업이 빨리 끝나더라도 최소 1분간 락 유지
- 다른 인스턴스에서 바로 재실행 방지
- 너무 빈번한 실행 방지

**lockAtMostFor (최대 락 시간):**
- 인스턴스가 크래시되어도 최대 10분 후 락 자동 해제
- 데드락 방지
- 다른 인스턴스에서 복구 가능

### 4. 실제 코드에서의 동작

**정상적인 경우:**
```java
// 1. 락 획득 시도
if (shedLockAcquired("VerificationStatSyncScheduler")) {
    try {
        // 2. 실제 스케줄러 로직 실행
        syncVerificationStats();
        
        // 3. 작업 완료 후 락 해제 (단, lockAtLeastFor 시간은 유지)
    } finally {
        // 4. lockAtLeastFor 시간 후 락 해제
        shedLockRelease("VerificationStatSyncScheduler");
    }
} else {
    // 5. 락 획득 실패 시 스케줄러 스킵
    log.info("다른 인스턴스에서 실행 중, 스킵");
}
```

**인스턴스 크래시된 경우:**
```java
// lockAtMostFor = "10m" 설정에 의해
// 10분 후 Redis TTL로 자동 락 해제
// 다른 인스턴스에서 정상적으로 스케줄러 재개
```

### 5. Redis Lua Script를 통한 원자성 보장

ShedLock은 내부적으로 Lua Script를 사용해 락 획득/해제의 원자성을 보장합니다:

```lua
-- 락 획득 Lua Script
local lockKey = KEYS[1]
local lockValue = ARGV[1] 
local ttl = ARGV[2]

if redis.call('exists', lockKey) == 0 then
    redis.call('set', lockKey, lockValue, 'EX', ttl)
    return 1
else 
    return 0
end
```

### 6. 장애 상황별 동작

**시나리오 1: 인스턴스 A가 락 획득 후 정상 완료**
```
A: 락 획득 → 작업 실행 (3초) → 락 해제 (lockAtLeastFor 1분 후)
B: 락 획득 실패 → 스킵
C: 락 획득 실패 → 스킵
```

**시나리오 2: 인스턴스 A가 락 획득 후 크래시**
```
A: 락 획득 → 작업 실행 중 크래시
B: 락 획득 실패 → 스킵 (10분간 대기)
C: 락 획득 실패 → 스킵 (10분간 대기)
10분 후: Redis TTL로 락 자동 해제 → 다음 스케줄링 시 B 또는 C가 실행
```

### 7. 성능상의 이점

**ShedLock 없는 경우:**
- 3개 인스턴스에서 동시 실행
- DB 조회 3번, Redis 업데이트 3번
- CPU/메모리 3배 사용

**ShedLock 적용한 경우:**
- 1개 인스턴스에서만 실행  
- DB 조회 1번, Redis 업데이트 1번
- CPU/메모리 사용량 1/3로 감소

### 8. Redis 키 관리

**생성되는 Redis 키:**
```
shedlock:VerificationStatSyncScheduler
shedlock:AnotherScheduler
shedlock:BatchJobScheduler
```

**키 네이밍 규칙:**
- prefix: "shedlock:" (설정에서 정의)
- name: 어노테이션의 name 속성값
- 각 스케줄러마다 독립적인 락
