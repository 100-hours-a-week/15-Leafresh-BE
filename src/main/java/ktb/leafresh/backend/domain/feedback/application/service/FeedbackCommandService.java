package ktb.leafresh.backend.domain.feedback.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import ktb.leafresh.backend.domain.feedback.application.assembler.FeedbackDtoAssembler;
import ktb.leafresh.backend.domain.feedback.infrastructure.dto.request.AiFeedbackCreationRequestDto;
import ktb.leafresh.backend.domain.feedback.infrastructure.publisher.GcpAiFeedbackPubSubPublisher;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.FeedbackErrorCode;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackCommandService {

    private final MemberRepository memberRepository;
    private final FeedbackDtoAssembler dtoAssembler;
//    private final FeedbackCreationClient feedbackCreationClient;
    private final GcpAiFeedbackPubSubPublisher feedbackPublisher;
    private final ObjectMapper objectMapper;

    public void handleFeedbackCreationRequest(Long memberId) {
        log.info("[피드백 생성 요청 시작] memberId={}", memberId);
//        log.info("주입된 FeedbackCreationClient 구현체 = {}", feedbackCreationClient.getClass().getName());

        Member member = validateMember(memberId);

        LocalDate monday = getLastWeekStart();
        LocalDate sunday = monday.plusDays(6);
        log.debug("[주차 계산] monday={}, sunday={}", monday, sunday);

        AiFeedbackCreationRequestDto requestDto = dtoAssembler.assemble(memberId, monday, sunday);

        int personalCount = requestDto.personalChallenges().size();
        int groupSubmissionCount = requestDto.groupChallenges().stream()
                .mapToInt(g -> g.submissions().size())
                .sum();
        int totalSubmissionCount = personalCount + groupSubmissionCount;

        log.debug("[인증 수 집계] personal={}, groupSubmission={}, totalSubmission={}",
                personalCount, groupSubmissionCount, totalSubmissionCount);

        if (totalSubmissionCount == 0) {
            log.warn("[인증 없음] memberId={}, 인증 기록 없음", memberId);
            throw new CustomException(FeedbackErrorCode.NO_CHALLENGE_ACTIVITY);
        }

        try {
            String prettyJson = objectMapper
                    .copy()
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .writeValueAsString(requestDto);

            log.info("[AI 요청 전송 request body]\n{}", prettyJson);

//            feedbackCreationClient.requestWeeklyFeedback(requestDto);
            feedbackPublisher.publishAsyncWithRetry(requestDto);
            log.info("[피드백 요청 완료] memberId={}", memberId);
        } catch (JsonProcessingException e) {
            log.error("[JSON 변환 실패]", e);
            throw new RuntimeException("JSON 직렬화 실패", e);
        } catch (Exception e) {
            log.error("[피드백 생성 요청 중 예외 발생]", e);
            throw e;
        }
    }

    private Member validateMember(Long memberId) {
        return memberRepository.findById(memberId)
                .filter(Member::getActivated)
                .orElseThrow(() -> {
                    log.warn("[멤버 유효성 실패] 비활성 사용자 또는 존재하지 않음: memberId={}", memberId);
                    return new CustomException(GlobalErrorCode.UNAUTHORIZED);
                });
    }

    private LocalDate getLastWeekStart() {
        return LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1);
    }
}
