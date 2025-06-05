package ktb.leafresh.backend.domain.challenge.group.presentation.controller;

import ktb.leafresh.backend.domain.challenge.group.application.service.*;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.*;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CustomUserDetails;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/group/{challengeId}")
public class GroupChallengeVerificationReadController {

    private final GroupChallengeVerificationReadService groupChallengeVerificationReadService;

    @GetMapping("/verifications")
    public ResponseEntity<ApiResponse<GroupChallengeVerificationListResponseDto>> getVerifications(
            @PathVariable Long challengeId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) String cursorTimestamp,
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if ((cursorId == null) != (cursorTimestamp == null)) {
            throw new CustomException(GlobalErrorCode.INVALID_CURSOR);
        }

        Long memberId = userDetails != null ? userDetails.getMemberId() : null;

        CursorPaginationResult<GroupChallengeVerificationSummaryDto> result =
                groupChallengeVerificationReadService.getVerifications(challengeId, cursorId, cursorTimestamp, size, memberId);

        return ResponseEntity.ok(ApiResponse.success(
                "단체 챌린지 인증 내역 조회에 성공했습니다.",
                GroupChallengeVerificationListResponseDto.from(result)
        ));
    }

    @GetMapping("/rules")
    public ResponseEntity<ApiResponse<GroupChallengeRuleResponseDto>> getGroupChallengeRules(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
        }

        GroupChallengeRuleResponseDto response = groupChallengeVerificationReadService.getChallengeRules(challengeId);
        return ResponseEntity.ok(ApiResponse.success("단체 챌린지 인증 규약 정보를 성공적으로 조회했습니다.", response));
    }
}
