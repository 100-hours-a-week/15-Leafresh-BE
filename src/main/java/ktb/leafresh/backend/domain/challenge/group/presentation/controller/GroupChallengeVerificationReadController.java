package ktb.leafresh.backend.domain.challenge.group.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import ktb.leafresh.backend.domain.challenge.group.application.service.*;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.*;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CurrentMemberId;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Group Challenge Verification Read", description = "단체 챌린지 인증 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/group/{challengeId}")
@Validated
public class GroupChallengeVerificationReadController {

  private final GroupChallengeVerificationReadService groupChallengeVerificationReadService;

  @GetMapping("/verifications")
  @Operation(summary = "단체 챌린지 인증 목록 조회", description = "특정 단체 챌린지의 인증 내역을 커서 기반 페이지네이션으로 조회합니다.")
  public ResponseEntity<ApiResponse<GroupChallengeVerificationListResponseDto>> getVerifications(
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
      @Parameter(description = "커서 ID") @RequestParam(required = false) Long cursorId,
      @Parameter(description = "커서 타임스탬프") @RequestParam(required = false) String cursorTimestamp,
      @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "12") @Min(1) @Max(50) int size,
      @CurrentMemberId Long memberId) {

    CursorPaginationResult<GroupChallengeVerificationSummaryDto> result =
        groupChallengeVerificationReadService.getVerifications(
            challengeId, cursorId, cursorTimestamp, size, memberId);

    return ResponseEntity.ok(
        ApiResponse.success(
            "특정 단체 챌린지 인증 내역 목록 조회에 성공했습니다.",
            GroupChallengeVerificationListResponseDto.from(result)));
  }

  @GetMapping("/rules")
  @Operation(summary = "단체 챌린지 인증 규약 조회", description = "단체 챌린지의 인증 규약 정보를 조회합니다.")
  public ResponseEntity<ApiResponse<GroupChallengeRuleResponseDto>> getGroupChallengeRules(
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId) {

    GroupChallengeRuleResponseDto response =
        groupChallengeVerificationReadService.getChallengeRules(challengeId);
    
    return ResponseEntity.ok(ApiResponse.success("단체 챌린지 인증 규약 정보를 성공적으로 조회했습니다.", response));
  }

  @GetMapping("/verifications/{verificationId}")
  @Operation(summary = "단체 챌린지 인증 상세 조회", description = "특정 단체 챌린지 인증의 상세 정보를 조회합니다.")
  public ResponseEntity<ApiResponse<GroupChallengeVerificationDetailResponseDto>> getVerificationDetail(
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
      @Parameter(description = "인증 ID") @PathVariable Long verificationId,
      @CurrentMemberId Long memberId) {
    
    GroupChallengeVerificationDetailResponseDto response =
        groupChallengeVerificationReadService.getVerificationDetail(challengeId, verificationId, memberId);

    return ResponseEntity.ok(ApiResponse.success("특정 단체 챌린지 인증 상세 정보를 조회했습니다.", response));
  }
}
