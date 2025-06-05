package ktb.leafresh.backend.domain.challenge.group.presentation.controller;

import ktb.leafresh.backend.domain.challenge.group.application.service.*;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.*;
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
import org.springframework.web.bind.annotation.*;

@Slf4j
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

        try {
            CursorPaginationResult<GroupChallengeVerificationSummaryDto> result =
                    groupChallengeVerificationReadService.getVerifications(challengeId, cursorId, cursorTimestamp, size, memberId);

            return ResponseEntity.ok(ApiResponse.success(
                    "특정 단체 챌린지 인증 내역 목록 조회에 성공했습니다.",
                    GroupChallengeVerificationListResponseDto.from(result)
            ));

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[인증 목록 조회 실패] challengeId={}, cursorId={}, error={}",
                    challengeId, cursorId, e.getMessage(), e);
            throw new CustomException(VerificationErrorCode.VERIFICATION_LIST_QUERY_FAILED);
        }
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

    @GetMapping("/verifications/{verificationId}")
    public ResponseEntity<ApiResponse<GroupChallengeVerificationDetailResponseDto>> getVerificationDetail(
            @PathVariable Long challengeId,
            @PathVariable Long verificationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails != null ? userDetails.getMemberId() : null;

        try {
            GroupChallengeVerificationDetailResponseDto response =
                    groupChallengeVerificationReadService.getVerificationDetail(challengeId, verificationId, memberId);

            return ResponseEntity.ok(ApiResponse.success("특정 단체 챌린지 인증 상세 정보를 조회했습니다.", response));

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[인증 상세 조회 실패] challengeId={}, verificationId={}, error={}",
                    challengeId, verificationId, e.getMessage(), e);

            throw new CustomException(VerificationErrorCode.VERIFICATION_DETAIL_QUERY_FAILED);
        }
    }
}
