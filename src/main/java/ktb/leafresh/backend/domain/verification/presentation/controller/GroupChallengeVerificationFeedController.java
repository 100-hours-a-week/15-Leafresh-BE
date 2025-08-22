package ktb.leafresh.backend.domain.verification.presentation.controller;

import ktb.leafresh.backend.domain.verification.application.service.GroupChallengeVerificationFeedService;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.GroupChallengeVerificationFeedListResponseDto;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.GroupChallengeVerificationFeedSummaryDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CustomUserDetails;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/group/verifications")
public class GroupChallengeVerificationFeedController {

  private final GroupChallengeVerificationFeedService feedService;

  @GetMapping
  public ResponseEntity<ApiResponse<GroupChallengeVerificationFeedListResponseDto>> getFeed(
      @RequestParam(required = false) Long cursorId,
      @RequestParam(required = false) String cursorTimestamp,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(required = false) String category,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    if ((cursorId == null) != (cursorTimestamp == null)) {
      throw new CustomException(GlobalErrorCode.INVALID_CURSOR);
    }

    Long memberId = userDetails != null ? userDetails.getMemberId() : null;

    try {
      CursorPaginationResult<GroupChallengeVerificationFeedSummaryDto> result =
          feedService.getGroupChallengeVerifications(
              cursorId, cursorTimestamp, size, category, memberId);

      return ResponseEntity.ok(
          ApiResponse.success(
              "단체 챌린지 인증 내역 목록 조회에 성공했습니다.",
              GroupChallengeVerificationFeedListResponseDto.from(result)));

    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error("[피드 조회 실패] cursorId={}, error={}", cursorId, e.getMessage(), e);
      throw new CustomException(VerificationErrorCode.VERIFICATION_LIST_QUERY_FAILED);
    }
  }
}
