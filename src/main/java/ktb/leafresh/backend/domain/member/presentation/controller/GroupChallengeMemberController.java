package ktb.leafresh.backend.domain.member.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import ktb.leafresh.backend.domain.challenge.group.application.service.GroupChallengeCreatedReadService;
import ktb.leafresh.backend.domain.challenge.group.application.service.GroupChallengeParticipationReadService;
import ktb.leafresh.backend.domain.challenge.group.application.service.GroupChallengeVerificationHistoryService;
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

@Tag(name = "Group Challenge Member", description = "회원의 단체 챌린지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/challenges/group")
@Validated
public class GroupChallengeMemberController {

  private final GroupChallengeParticipationReadService groupChallengeParticipationReadService;
  private final GroupChallengeVerificationHistoryService groupChallengeVerificationHistoryService;
  private final GroupChallengeCreatedReadService groupChallengeCreatedReadService;

  @GetMapping("/creations")
  @Operation(summary = "생성한 단체 챌린지 목록 조회", description = "회원이 생성한 단체 챌린지를 커서 기반으로 조회합니다.")
  public ResponseEntity<ApiResponse<CreatedGroupChallengeListResponseDto>> getCreatedChallenges(
      @CurrentMemberId Long memberId,
      @Parameter(description = "커서 ID") @RequestParam(required = false) Long cursorId,
      @Parameter(description = "커서 타임스탬프") @RequestParam(required = false) String cursorTimestamp,
      @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "12") @Min(1) @Max(50)
          int size) {

    CursorPaginationResult<CreatedGroupChallengeSummaryResponseDto> result =
        groupChallengeCreatedReadService.getCreatedChallengesByMember(
            memberId, cursorId, cursorTimestamp, size);

    return ResponseEntity.ok(
        ApiResponse.success(
            "생성한 단체 챌린지 목록 조회에 성공했습니다.", CreatedGroupChallengeListResponseDto.from(result)));
  }

  @GetMapping("/participations/count")
  @Operation(summary = "참여한 단체 챌린지 카운트 조회", description = "회원이 참여한 단체 챌린지의 상태별 카운트를 조회합니다.")
  public ResponseEntity<ApiResponse<GroupChallengeParticipationCountResponseDto>>
      getParticipationCounts(@CurrentMemberId Long memberId) {

    GroupChallengeParticipationCountResponseDto response =
        groupChallengeParticipationReadService.getParticipationCounts(memberId);

    return ResponseEntity.ok(ApiResponse.success("참여한 단체 챌린지 카운트를 성공적으로 조회했습니다.", response));
  }

  @GetMapping("/participations")
  @Operation(summary = "참여한 단체 챌린지 목록 조회", description = "회원이 참여한 단체 챌린지를 status별로 커서 기반으로 조회합니다.")
  public ResponseEntity<ApiResponse<GroupChallengeParticipationListResponseDto>>
      getParticipatedChallenges(
          @CurrentMemberId Long memberId,
          @Parameter(description = "챌린지 상태") @RequestParam @NotBlank String status,
          @Parameter(description = "커서 ID") @RequestParam(required = false) Long cursorId,
          @Parameter(description = "커서 타임스탬프") @RequestParam(required = false)
              String cursorTimestamp,
          @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "12") @Min(1) @Max(50)
              int size) {

    var response =
        groupChallengeParticipationReadService.getParticipatedChallenges(
            memberId, status, cursorId, cursorTimestamp, size);

    return ResponseEntity.ok(ApiResponse.success("참여한 단체 챌린지 목록을 성공적으로 조회했습니다.", response));
  }

  @GetMapping("/participations/{challengeId}/verifications")
  @Operation(summary = "참여 챌린지 인증내역 일별 조회", description = "참여한 챌린지의 인증내역을 일자별로 제공합니다.")
  public ResponseEntity<ApiResponse<GroupChallengeVerificationHistoryResponseDto>>
      getVerificationHistory(
          @CurrentMemberId Long memberId,
          @Parameter(description = "챌린지 ID") @PathVariable Long challengeId) {

    GroupChallengeVerificationHistoryResponseDto response =
        groupChallengeVerificationHistoryService.getVerificationHistory(memberId, challengeId);

    return ResponseEntity.ok(ApiResponse.success("단체 챌린지 인증 내역을 성공적으로 조회했습니다.", response));
  }
}
