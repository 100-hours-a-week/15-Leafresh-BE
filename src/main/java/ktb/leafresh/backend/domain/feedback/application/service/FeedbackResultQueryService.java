package ktb.leafresh.backend.domain.feedback.application.service;

import ktb.leafresh.backend.domain.feedback.presentation.dto.response.FeedbackResponseDto;
import ktb.leafresh.backend.domain.feedback.infrastructure.repository.FeedbackRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackResultQueryService {

  private final MemberRepository memberRepository;
  private final FeedbackRepository feedbackRepository;
  private final RedisTemplate<String, Object> redisTemplate;

  public FeedbackResponseDto getFeedbackResult(Long memberId) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(GlobalErrorCode.UNAUTHORIZED));

    if (!member.getActivated()) {
      throw new CustomException(GlobalErrorCode.ACCESS_DENIED);
    }

    log.info("[피드백 결과 단건 조회] memberId={}", memberId);
    return getLatestFeedback(member);
  }

  private FeedbackResponseDto getLatestFeedback(Member member) {
    String key = "feedback:result:" + member.getId();
    String cached = (String) redisTemplate.opsForValue().get(key);

    if (cached != null) {
      log.info("[Redis 캐시 히트] memberId={}, content={}", member.getId(), cached);
      return new FeedbackResponseDto(cached);
    }

    log.info("[Redis 캐시 미스] DB 조회 시도 memberId={}", member.getId());
    LocalDateTime lastWeekMonday =
        LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1).atStartOfDay();
    return feedbackRepository
        .findFeedbackByMemberAndWeekMonday(member, lastWeekMonday)
        .map(fb -> new FeedbackResponseDto(fb.getContent()))
        .orElse(new FeedbackResponseDto(null));
  }
}
