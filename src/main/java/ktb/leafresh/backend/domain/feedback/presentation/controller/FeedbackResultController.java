package ktb.leafresh.backend.domain.feedback.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.feedback.application.service.FeedbackResultQueryService;
import ktb.leafresh.backend.domain.feedback.application.service.FeedbackResultService;
import ktb.leafresh.backend.domain.feedback.presentation.dto.request.FeedbackResultRequestDto;
import ktb.leafresh.backend.domain.feedback.presentation.dto.response.FeedbackResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Feedback Result", description = "피드백 결과 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/feedback")
@Validated
public class FeedbackResultController {

  private final FeedbackResultService feedbackResultService;
  private final FeedbackResultQueryService feedbackResultQueryService;

  @PostMapping("/result")
  @Operation(summary = "피드백 결과 수신", description = "외부 AI 서비스로부터 생성된 피드백 결과를 수신합니다. (외부 서비스 전용)")
  public ResponseEntity<ApiResponse<Void>> receiveFeedbackResult(
      @Valid @RequestBody FeedbackResultRequestDto requestDto) {

    feedbackResultService.receiveFeedback(requestDto);
    return ResponseEntity.ok(ApiResponse.success("피드백 결과 수신 완료"));
  }

  @GetMapping("/result")
  @Operation(summary = "피드백 결과 조회", description = "요청한 피드백의 생성 결과를 조회합니다.")
  public ResponseEntity<ApiResponse<FeedbackResponseDto>> getFeedbackResult(
      @CurrentMemberId Long memberId) {

    FeedbackResponseDto response = feedbackResultQueryService.getFeedbackResult(memberId);

    if (response.getContent() == null) {
      return ResponseEntity.noContent().build();
    }

    return ResponseEntity.ok(ApiResponse.success("피드백 결과 수신 완료", response));
  }
}
