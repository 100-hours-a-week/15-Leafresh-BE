# 분산 스케줄러 테스트 가이드

## 🚀 테스트 실행 방법

### 1단계: 첫 번째 인스턴스 실행
1. IntelliJ에서 `LeafreshBackendApplication` 실행
2. VM Options에 추가: `-Dspring.profiles.active=scheduler-test -Dserver.port=8080`
3. 또는 Run Configuration 수정:
   - Active profiles: `scheduler-test`
   - Program arguments: `--server.port=8080`

### 2단계: 두 번째 인스턴스 실행
1. 새로운 Run Configuration 생성 (기존 것 복사)
2. VM Options: `-Dspring.profiles.active=scheduler-test -Dserver.port=8081`
3. 실행

### 3단계: 세 번째 인스턴스 실행 (선택사항)
1. 또 다른 Run Configuration 생성
2. VM Options: `-Dspring.profiles.active=scheduler-test -Dserver.port=8082`
3. 실행

## 📊 결과 확인 방법

### 방법 1: 로그 확인
각 콘솔창에서 다음 로그 패턴 확인:
- `🔥 [인스턴스 xxx] 스케줄러 실행 시작!` (ShedLock 적용)
- `🚨 [NO-LOCK] [인스턴스 xxx] 락 없는 스케줄러 실행!` (ShedLock 미적용)

### 방법 2: HTTP API로 결과 확인
```bash
# 실행 결과 조회
curl http://localhost:8080/test/scheduler/results | jq

# 테스트 데이터 초기화
curl http://localhost:8080/test/scheduler/clear
```

## 🎯 예상 결과

### ShedLock 적용된 스케줄러 (`🔥` 로그)
- 인스턴스가 3개여도 **동시에는 1개만 실행**
- 30초마다 1번씩만 실행됨

### ShedLock 없는 스케줄러 (`🚨` 로그)
- 인스턴스 수만큼 **중복 실행**
- 30초마다 3번 실행됨 (3개 인스턴스인 경우)

## 📈 테스트 시나리오

1. **2-3분 실행** 후 로그 확인
2. `/test/scheduler/results` API로 정량적 비교
3. 다음과 같은 결과 예상:
   ```json
   {
     "with_shedlock": {
       "total_executions": 4,
       "unique_instances": 1
     },
     "without_shedlock": {
       "total_executions": 12,
       "unique_instances": 3
     }
   }
   ```

## 🔧 문제 해결

### Redis 연결 오류
- 로컬 Redis가 실행 중인지 확인
- `docker run -d -p 6379:6379 redis:latest`

### ShedLock 작동하지 않음
- DB에 `shedlock` 테이블이 있는지 확인
- 애플리케이션 로그에서 ShedLock 관련 오류 확인

### 포트 충돌
- 각 인스턴스마다 다른 포트 사용 확인
- `netstat -an | grep 808[0-2]` 로 포트 사용 확인
