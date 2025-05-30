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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackReadService {

    private final FeedbackRepository feedbackRepository;
    private final MemberRepository memberRepository;

    public FeedbackResponseDto getFeedbackForLastWeek(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.UNAUTHORIZED));

        if (!member.getActivated()) {
            throw new CustomException(GlobalErrorCode.ACCESS_DENIED);
        }

        LocalDateTime lastWeekMonday = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1).atStartOfDay();

        log.info("[피드백 주차 계산] memberId: {}, weekMonday: {}", memberId, lastWeekMonday);

        try {
            return feedbackRepository.findFeedbackByMemberAndWeekMonday(member, lastWeekMonday)
                    .map(feedback -> {
                        log.info("[피드백 조회 성공] memberId: {}, content: {}", member.getId(), feedback.getContent());
                        return new FeedbackResponseDto(feedback.getContent());
                    })
                    .orElseGet(() -> {
                        log.info("[피드백 없음] memberId: {}", member.getId());
                        return new FeedbackResponseDto(null);
                    });
        } catch (Exception e) {
            log.error("[DB 조회 실패] 피드백 조회 중 예외 발생", e);
            throw new CustomException(FeedbackErrorCode.FEEDBACK_SERVER_ERROR);
        }
    }
}
