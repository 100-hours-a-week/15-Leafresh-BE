package ktb.leafresh.backend.domain.feedback.application.service;

import ktb.leafresh.backend.domain.feedback.domain.event.FeedbackCreatedEvent;
import ktb.leafresh.backend.domain.feedback.infrastructure.repository.FeedbackRepository;
import ktb.leafresh.backend.domain.feedback.presentation.dto.request.FeedbackResultRequestDto;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.global.exception.FeedbackErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackResultService {

  private final MemberRepository memberRepository;
  private final FeedbackRepository feedbackRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void receiveFeedback(FeedbackResultRequestDto dto) {
    log.info("[피드백 저장 사전 검증] memberId={}", dto.memberId());

    Member member =
        memberRepository
            .findById(dto.memberId())
            .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

    LocalDateTime weekMonday = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1).atStartOfDay();

    boolean exists = feedbackRepository.existsByMemberIdAndWeekMonday(member.getId(), weekMonday);
    if (exists) {
      throw new CustomException(FeedbackErrorCode.ALREADY_FEEDBACK_EXISTS);
    }

    // 이벤트 발행 (실제 저장은 AFTER_COMMIT에서 수행)
    eventPublisher.publishEvent(new FeedbackCreatedEvent(dto.memberId(), dto.content()));
    log.info("[피드백 저장 이벤트 발행 완료] memberId={}", dto.memberId());
  }
}
