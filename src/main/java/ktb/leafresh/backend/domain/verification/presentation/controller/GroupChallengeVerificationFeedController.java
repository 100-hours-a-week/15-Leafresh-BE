package ktb.leafresh.backend.domain.verification.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import ktb.leafresh.backend.domain.verification.application.service.GroupChallengeVerificationFeedService;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.GroupChallengeVerificationFeedListResponseDto;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.GroupChallengeVerificationFeedSummaryDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CurrentMemberId;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Group Challenge Verification Feed", description = "단체 챌린지 인증 피드 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/group/verifications")
@Validated
public class GroupChallengeVerificationFeedController {

  private final GroupChallengeVerificationFeedService feedService;

  @GetMapping
  @Operation(summary = "인증 피드 조회", description = "단체 챌린지 인증 피드를 커서 기반 페이지네이션으로 조회합니다.")
  public ResponseEntity<ApiResponse<GroupChallengeVerificationFeedListResponseDto>> getFeed(
      @Parameter(description = "커서 ID") @RequestParam(required = false) Long cursorId,
      @Parameter(description = "커서 타임스탬프") @RequestParam(required = false) String cursorTimestamp,
      @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "12") @Min(1) @Max(50)
          int size,
      @Parameter(description = "카테고리 필터") @RequestParam(required = false) String category,
      @CurrentMemberId Long memberId) {

    CursorPaginationResult<GroupChallengeVerificationFeedSummaryDto> result =
        feedService.getGroupChallengeVerifications(
            cursorId, cursorTimestamp, size, category, memberId);

    return ResponseEntity.ok(
        ApiResponse.success(
            "단체 챌린지 인증 내역 목록 조회에 성공했습니다.",
            GroupChallengeVerificationFeedListResponseDto.from(result)));
  }
}
