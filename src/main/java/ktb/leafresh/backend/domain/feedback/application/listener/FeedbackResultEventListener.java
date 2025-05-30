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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedbackResultEventListener {

    private final FeedbackRepository feedbackRepository;
    private final MemberRepository memberRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(FeedbackCreatedEvent event) {
        try {
            log.info("[이벤트 수신] 피드백 저장 시작 memberId={}", event.memberId());

            Member member = memberRepository.findById(event.memberId())
                    .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

            LocalDateTime weekMonday = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1).atStartOfDay();

            Feedback feedback = Feedback.of(member, event.content(), weekMonday);
            feedbackRepository.save(feedback);

            log.info("[피드백 저장 완료] memberId={}", event.memberId());

        } catch (Exception e) {
            log.error("[피드백 저장 실패] error={}", e.getMessage(), e);
            throw new CustomException(FeedbackErrorCode.FEEDBACK_SAVE_FAIL);
        }
    }
}
