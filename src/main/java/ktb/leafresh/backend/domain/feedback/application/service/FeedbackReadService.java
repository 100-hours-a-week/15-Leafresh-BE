package ktb.leafresh.backend.domain.feedback.application.service;

import ktb.leafresh.backend.domain.feedback.presentation.dto.response.FeedbackResponseDto;
import ktb.leafresh.backend.domain.feedback.infrastructure.repository.FeedbackRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.FeedbackErrorCode;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackReadService {

    private final FeedbackRepository feedbackRepository;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public FeedbackResponseDto getFeedbackForLastWeek(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.UNAUTHORIZED));

        if (!member.getActivated()) {
            throw new CustomException(GlobalErrorCode.ACCESS_DENIED);
        }

        String key = generateKey(memberId);
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            log.info("[Redis 캐시 히트 - 일반 조회] memberId={}, content={}", memberId, cached);
            return new FeedbackResponseDto((String) cached);
        }

        LocalDateTime lastWeekMonday = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1).atStartOfDay();
        log.info("[Redis 캐시 미스 - 일반 조회] memberId={}, weekMonday={}", memberId, lastWeekMonday);

        try {
            return feedbackRepository.findFeedbackByMemberAndWeekMonday(member, lastWeekMonday)
                    .map(feedback -> {
                        String content = feedback.getContent();
                        log.info("[DB 피드백 조회 성공] memberId={}, content={}", memberId, content);

                        // 캐시 저장
                        LocalDate sunday = lastWeekMonday.toLocalDate().plusDays(6);
                        LocalDateTime expireAt = sunday.atTime(23, 59, 59);
                        long ttlSeconds = Math.max(Duration.between(LocalDateTime.now(), expireAt).getSeconds(), 0);
                        if (ttlSeconds > 0) {
                            redisTemplate.opsForValue().set(key, content, ttlSeconds, TimeUnit.SECONDS);
                            log.info("[Redis 캐시 저장 완료 - 일반 조회] key={}, ttl(s)={}", key, ttlSeconds);
                        }

                        return new FeedbackResponseDto(content);
                    })
                    .orElseGet(() -> {
                        log.info("[DB 피드백 없음] memberId={}", memberId);
                        return new FeedbackResponseDto(null);
                    });

        } catch (Exception e) {
            log.error("[DB 조회 실패] 피드백 조회 중 예외 발생", e);
            throw new CustomException(FeedbackErrorCode.FEEDBACK_SERVER_ERROR);
        }
    }

    private String generateKey(Long memberId) {
        return "feedback:result:" + memberId;
    }
}
