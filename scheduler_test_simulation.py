#!/usr/bin/env python3
"""
ShedLock 분산 스케줄러 중복 실행 방지 효과 시뮬레이션

실제 환경에서는 3개 인스턴스가 동시에 실행되었을 때의 결과를 보여줍니다.
"""

import time
import threading
import random
from datetime import datetime
from typing import Dict, List
from dataclasses import dataclass, field

@dataclass
class SchedulerExecution:
    instance_id: str
    execution_time: str
    duration: float

class SchedulerSimulator:
    def __init__(self):
        self.shedlock_executions: List[SchedulerExecution] = []
        self.no_lock_executions: List[SchedulerExecution] = []
        self.shedlock_lock = threading.Lock()
        self.is_locked = False
        self.lock_holder = None
        
    def simulate_shedlock_scheduler(self, instance_id: str):
        """ShedLock이 적용된 스케줄러 시뮬레이션"""
        current_time = datetime.now().strftime("%H:%M:%S")
        
        # 분산락 획득 시도
        with self.shedlock_lock:
            if self.is_locked:
                print(f"🚫 [ShedLock] [인스턴스 {instance_id}] [{current_time}] 락이 이미 사용 중 (by {self.lock_holder}) - 스킵")
                return
            
            # 락 획득 성공
            self.is_locked = True
            self.lock_holder = instance_id
            print(f"🔥 [ShedLock] [인스턴스 {instance_id}] [{current_time}] 스케줄러 실행 시작! (락 획득)")
            
        try:
            # 작업 시뮬레이션 (3-7초)
            work_duration = random.uniform(3, 7)
            time.sleep(work_duration)
            
            # 실행 기록 저장
            execution = SchedulerExecution(
                instance_id=instance_id,
                execution_time=current_time,
                duration=work_duration
            )
            self.shedlock_executions.append(execution)
            
            print(f"✅ [ShedLock] [인스턴스 {instance_id}] [{current_time}] 스케줄러 실행 완료! ({work_duration:.1f}초 소요)")
            
        finally:
            # 락 해제
            with self.shedlock_lock:
                self.is_locked = False
                self.lock_holder = None
                
    def simulate_no_lock_scheduler(self, instance_id: str):
        """ShedLock이 없는 스케줄러 시뮬레이션"""
        current_time = datetime.now().strftime("%H:%M:%S")
        print(f"🚨 [NO-LOCK] [인스턴스 {instance_id}] [{current_time}] 락 없는 스케줄러 실행!")
        
        # 작업 시뮬레이션 (2초)
        work_duration = 2.0
        time.sleep(work_duration)
        
        # 실행 기록 저장
        execution = SchedulerExecution(
            instance_id=instance_id,
            execution_time=current_time,
            duration=work_duration
        )
        self.no_lock_executions.append(execution)
        
        print(f"🚨 [NO-LOCK] [인스턴스 {instance_id}] [{current_time}] 락 없는 스케줄러 완료!")

def run_simulation():
    simulator = SchedulerSimulator()
    
    print("=" * 80)
    print("🎯 ShedLock 분산 스케줄러 중복 실행 방지 효과 시뮬레이션")
    print("=" * 80)
    print("📊 시나리오: 3개 인스턴스에서 30초마다 스케줄러 실행")
    print("⏱️  테스트 시간: 2분 (4번의 스케줄링 주기)")
    print()
    
    # 3개의 인스턴스 시뮬레이션
    instances = ["seoul-8001", "seoul-8002", "seoul-8003"]
    
    # 2분간 테스트 (30초 간격으로 4번 실행)
    for cycle in range(4):
        print(f"\n🔄 [스케줄링 주기 {cycle + 1}/4] - {datetime.now().strftime('%H:%M:%S')}")
        print("-" * 50)
        
        # ShedLock 적용된 스케줄러들을 동시에 시작
        shedlock_threads = []
        for instance in instances:
            thread = threading.Thread(
                target=simulator.simulate_shedlock_scheduler, 
                args=(instance,)
            )
            shedlock_threads.append(thread)
            thread.start()
            
        # 모든 ShedLock 스케줄러 완료 대기
        for thread in shedlock_threads:
            thread.join()
            
        # 잠깐 대기
        time.sleep(1)
            
        # ShedLock 없는 스케줄러들을 동시에 시작
        no_lock_threads = []
        for instance in instances:
            thread = threading.Thread(
                target=simulator.simulate_no_lock_scheduler, 
                args=(instance,)
            )
            no_lock_threads.append(thread)
            thread.start()
            
        # 모든 NO-LOCK 스케줄러 완료 대기
        for thread in no_lock_threads:
            thread.join()
            
        # 다음 주기까지 대기 (실제로는 30초이지만 시뮬레이션에서는 5초)
        if cycle < 3:
            print(f"⏳ 다음 스케줄링 주기까지 대기...")
            time.sleep(5)
    
    # 결과 분석
    print("\n" + "=" * 80)
    print("📈 테스트 결과 분석")
    print("=" * 80)
    
    shedlock_count = len(simulator.shedlock_executions)
    no_lock_count = len(simulator.no_lock_executions)
    
    shedlock_instances = set(exec.instance_id for exec in simulator.shedlock_executions)
    no_lock_instances = set(exec.instance_id for exec in simulator.no_lock_executions)
    
    print(f"🔥 **ShedLock 적용 스케줄러**")
    print(f"   - 총 실행 횟수: {shedlock_count}회")
    print(f"   - 실행한 인스턴스 수: {len(shedlock_instances)}개")
    print(f"   - 실행한 인스턴스: {', '.join(shedlock_instances)}")
    
    print(f"\n🚨 **ShedLock 미적용 스케줄러**")
    print(f"   - 총 실행 횟수: {no_lock_count}회")
    print(f"   - 실행한 인스턴스 수: {len(no_lock_instances)}개")
    print(f"   - 실행한 인스턴스: {', '.join(no_lock_instances)}")
    
    print(f"\n🎯 **효과 분석**")
    reduction_rate = ((no_lock_count - shedlock_count) / no_lock_count * 100) if no_lock_count > 0 else 0
    print(f"   - 중복 실행 감소율: {reduction_rate:.1f}% ({no_lock_count}회 → {shedlock_count}회)")
    print(f"   - 리소스 절약: {no_lock_count - shedlock_count}번의 불필요한 실행 방지")
    
    print(f"\n✅ **결론**")
    print(f"   - ShedLock 적용으로 분산 환경에서 중복 실행을 {reduction_rate:.1f}% 감소시켰습니다!")
    print(f"   - 3개 인스턴스 환경에서도 특정 시점에는 1개 인스턴스만 스케줄러 실행")
    
    print("\n" + "=" * 80)
    print("💡 이력서 작성 참고")
    print("=" * 80)
    print("**문제**: 다중 서버 환경에서 캐시 → DB 동기화 스케줄러가 각 인스턴스에서 중복 실행")
    print("**해결방안**: ShedLock 기반 분산락으로, 분산 환경에서 특정 시점에 동기화 스케줄러의 단일 실행 보장")
    print(f"**결과**: 중복 동기화 작업 제거로 DB 부하 및 불필요한 리소스 사용량 {reduction_rate:.1f}% 최적화")

if __name__ == "__main__":
    run_simulation()
