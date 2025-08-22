package ktb.leafresh.backend.global.scheduler;

import ktb.leafresh.backend.domain.challenge.group.application.service.EventChallengeInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventChallengeScheduler {

  private final EventChallengeInitService eventChallengeInitService;

  // 앱 시작 시 한 번 실행
  @EventListener(ApplicationReadyEvent.class)
  public void initOnAppStart() {
    log.info("[EventChallengeScheduler] 앱 시작 시 이벤트 챌린지 등록 시도");
    eventChallengeInitService.registerCurrentYearEventChallengesIfNotExists();
  }

  // 매년 1월 1일 00:00에 자동 실행
  @Scheduled(cron = "0 0 0 1 1 *", zone = "Asia/Seoul")
  public void scheduleYearlyChallenges() {
    log.info("[EventChallengeScheduler] 1월 1일 자정, 이벤트 챌린지 등록 스케줄 실행");
    eventChallengeInitService.registerCurrentYearEventChallengesIfNotExists();
  }
}
