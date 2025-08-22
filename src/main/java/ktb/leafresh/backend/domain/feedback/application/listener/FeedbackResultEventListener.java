package ktb.leafresh.backend.domain.feedback.application.listener;

import ktb.leafresh.backend.domain.feedback.domain.entity.Feedback;
import ktb.leafresh.backend.domain.feedback.domain.event.FeedbackCreatedEvent;
import ktb.leafresh.backend.domain.feedback.infrastructure.repository.FeedbackRepository;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.global.exception.FeedbackErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedbackResultEventListener {

  private final FeedbackRepository feedbackRepository;
  private final MemberRepository memberRepository;
  private final RedisTemplate<String, Object> redisTemplate;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(FeedbackCreatedEvent event) {
    try {
      log.info("[이벤트 수신] 피드백 저장 시작 memberId={}", event.memberId());

      Member member =
          memberRepository
              .findById(event.memberId())
              .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

      LocalDateTime weekMonday =
          LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1).atStartOfDay();
      Feedback feedback = Feedback.of(member, event.content(), weekMonday);

      feedbackRepository.save(feedback);
      log.info("[피드백 저장 완료] memberId={}", event.memberId());

      // Redis 캐싱 (저장 시점 기준 이번 주 일요일 23:59:59까지 캐싱)
      String key = generateKey(event.memberId());
      LocalDate sunday = LocalDate.now().with(DayOfWeek.SUNDAY);
      LocalDateTime expireAt = sunday.atTime(23, 59, 59);
      Duration duration = Duration.between(LocalDateTime.now(), expireAt);
      long ttlSeconds = Math.max(duration.getSeconds(), 0);

      // TTL이 0 이하인 경우 캐싱 생략
      if (ttlSeconds <= 0) {
        log.warn("[Redis 캐싱 생략] 이미 만료된 시간입니다. key={}", key);
        return;
      }

      redisTemplate.opsForValue().set(key, event.content(), ttlSeconds, TimeUnit.SECONDS);
      log.info("[Redis 저장 완료] key={}, ttl(s)={}", key, ttlSeconds);

    } catch (Exception e) {
      log.error("[피드백 저장 실패] error={}", e.getMessage(), e);
      throw new CustomException(FeedbackErrorCode.FEEDBACK_SAVE_FAIL);
    }
  }

  private String generateKey(Long memberId) {
    return "feedback:result:" + memberId;
  }
}
