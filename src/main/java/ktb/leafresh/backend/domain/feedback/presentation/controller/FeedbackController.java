package ktb.leafresh.backend.domain.feedback.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.feedback.application.service.FeedbackCommandService;
import ktb.leafresh.backend.domain.feedback.application.service.FeedbackReadService;
import ktb.leafresh.backend.domain.feedback.presentation.dto.request.FeedbackRequestDto;
import ktb.leafresh.backend.domain.feedback.presentation.dto.response.FeedbackResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Feedback", description = "피드백 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/feedback")
@Validated
public class FeedbackController {

  private final FeedbackReadService feedbackReadService;
  private final FeedbackCommandService feedbackCommandService;

  @GetMapping
  @Operation(summary = "주간 피드백 조회", description = "지난주 활동을 기반으로 생성된 피드백을 조회합니다.")
  public ResponseEntity<ApiResponse<FeedbackResponseDto>> getWeeklyFeedback(
      @CurrentMemberId Long memberId) {

    FeedbackResponseDto responseDto = feedbackReadService.getFeedbackForLastWeek(memberId);
    return ResponseEntity.ok(ApiResponse.success("지난주 피드백을 성공적으로 조회했습니다.", responseDto));
  }

  @PostMapping
  @Operation(summary = "주간 피드백 생성 요청", description = "지난주 활동을 기반으로 피드백 생성을 요청합니다.")
  public ResponseEntity<ApiResponse<Void>> requestWeeklyFeedback(
      @CurrentMemberId Long memberId,
      @Valid @RequestBody FeedbackRequestDto requestDto) {

    feedbackCommandService.handleFeedbackCreationRequest(memberId);
    
    return ResponseEntity.status(202)
        .body(ApiResponse.success("피드백 요청이 정상적으로 접수되었습니다. 결과는 곧 제공됩니다."));
  }
}
