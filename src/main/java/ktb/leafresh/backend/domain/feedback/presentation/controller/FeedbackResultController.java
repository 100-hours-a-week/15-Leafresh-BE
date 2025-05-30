package ktb.leafresh.backend.domain.feedback.presentation.controller;

import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.feedback.application.service.FeedbackResultQueryService;
import ktb.leafresh.backend.domain.feedback.application.service.FeedbackResultService;
import ktb.leafresh.backend.domain.feedback.presentation.dto.request.FeedbackResultRequestDto;
import ktb.leafresh.backend.domain.feedback.presentation.dto.response.FeedbackResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.FeedbackErrorCode;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/feedback")
@Slf4j
public class FeedbackResultController {

    private final FeedbackResultService feedbackResultService;
    private final FeedbackResultQueryService feedbackResultQueryService;

    @PostMapping("/result")
    public ResponseEntity<ApiResponse<Void>> receiveFeedbackResult(
            @Valid @RequestBody FeedbackResultRequestDto requestDto) {

        log.info("[피드백 결과 수신 요청] memberId={}, content={}", requestDto.memberId(), requestDto.content());

        feedbackResultService.receiveFeedback(requestDto);
        return ResponseEntity.ok(ApiResponse.success("피드백 결과 수신 완료"));
    }

    @GetMapping("/result")
    public ResponseEntity<ApiResponse<FeedbackResponseDto>> getFeedbackResult(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
        }

        Long memberId = userDetails.getMemberId();
        log.info("[피드백 결과 조회 요청] memberId={}", memberId);

        FeedbackResponseDto response = feedbackResultQueryService.waitForFeedback(memberId);

        if (response.getContent() == null) {
            log.info("[피드백 결과 없음] memberId={}", memberId);
            throw new CustomException(FeedbackErrorCode.FEEDBACK_NOT_READY);
        }

        log.info("[피드백 결과 반환] memberId={}, content={}", memberId, response.getContent());
        return ResponseEntity.ok(ApiResponse.success("피드백 결과 수신 완료", response));
    }
}
