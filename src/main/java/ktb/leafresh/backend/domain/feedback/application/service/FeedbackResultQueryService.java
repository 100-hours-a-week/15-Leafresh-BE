package ktb.leafresh.backend.domain.feedback.application.service;

import ktb.leafresh.backend.domain.feedback.presentation.dto.response.FeedbackResponseDto;
import ktb.leafresh.backend.domain.feedback.infrastructure.repository.FeedbackRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.FeedbackErrorCode;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.util.polling.FeedbackPollingExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final FeedbackPollingExecutor feedbackPollingExecutor;

    public FeedbackResponseDto waitForFeedback(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(GlobalErrorCode.UNAUTHORIZED));

        if (!member.getActivated()) {
            throw new CustomException(GlobalErrorCode.ACCESS_DENIED);
        }

        log.info("[피드백 롱폴링 시작] memberId={}", memberId);

        return feedbackPollingExecutor.poll(() -> getLatestFeedback(member));
    }

    private FeedbackResponseDto getLatestFeedback(Member member) {
        LocalDateTime thisWeekMonday = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        return feedbackRepository
                .findFeedbackByMemberAndWeekMonday(member, thisWeekMonday.minusWeeks(1))
                .map(fb -> new FeedbackResponseDto(fb.getContent()))
                .orElse(new FeedbackResponseDto(null));
    }
}
