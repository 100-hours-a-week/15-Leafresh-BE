package ktb.leafresh.backend.domain.feedback.presentation.controller;

import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.feedback.application.service.FeedbackCommandService;
import ktb.leafresh.backend.domain.feedback.application.service.FeedbackReadService;
import ktb.leafresh.backend.domain.feedback.presentation.dto.request.FeedbackRequestDto;
import ktb.leafresh.backend.domain.feedback.presentation.dto.response.FeedbackResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/feedback")
public class FeedbackController {

    private final FeedbackReadService feedbackReadService;
    private final FeedbackCommandService feedbackCommandService;

    @GetMapping
    public ResponseEntity<ApiResponse<FeedbackResponseDto>> getWeeklyFeedback(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getMemberId();
        log.info("[피드백 조회 요청] memberId: {}", memberId);

        try {
            FeedbackResponseDto responseDto = feedbackReadService.getFeedbackForLastWeek(memberId);
            return ResponseEntity.ok(ApiResponse.success("지난주 피드백을 성공적으로 조회했습니다.", responseDto));
        } catch (CustomException e) {
            log.warn("[피드백 조회 실패] error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[서버 오류] 피드백 조회 중 예외 발생", e);
            throw new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> requestWeeklyFeedback(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody FeedbackRequestDto requestDto
    ) {
        if (userDetails == null) throw new CustomException(GlobalErrorCode.UNAUTHORIZED);

        Long memberId = userDetails.getMemberId();
        log.info("[피드백 생성 요청] memberId={}, reason={}", memberId, requestDto.reason());

        try {
            feedbackCommandService.handleFeedbackCreationRequest(memberId);
            return ResponseEntity.status(202)
                    .body(ApiResponse.success("피드백 요청이 정상적으로 접수되었습니다. 결과는 곧 제공됩니다."));
        } catch (CustomException e) {
            log.warn("[피드백 생성 실패] error={}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[서버 오류] 피드백 생성 요청 중 예외 발생", e);
            throw new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
