package ktb.leafresh.backend.domain.verification.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.verification.application.service.GroupChallengeVerificationResultQueryService;
import ktb.leafresh.backend.domain.verification.application.service.GroupChallengeVerificationSubmitService;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.GroupChallengeVerificationRequestDto;
import ktb.leafresh.backend.domain.verification.presentation.util.ChallengeStatusMessageResolver;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Group Challenge Verification Submit", description = "단체 챌린지 인증 제출 API")
@RestController
@RequestMapping("/api/challenges/group")
@RequiredArgsConstructor
@Validated
public class GroupChallengeVerificationSubmitController {

  private final GroupChallengeVerificationSubmitService submitService;
  private final GroupChallengeVerificationResultQueryService resultQueryService;

  @PostMapping("/{challengeId}/verifications")
  @Operation(summary = "단체 챌린지 인증 제출", description = "단체 챌린지에 대한 인증을 제출합니다.")
  public ResponseEntity<ApiResponse<Void>> submitVerification(
      @CurrentMemberId Long memberId,
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
      @Valid @RequestBody GroupChallengeVerificationRequestDto requestDto) {

    submitService.submit(memberId, challengeId, requestDto);

    return ResponseEntity.ok(ApiResponse.success("단체 챌린지 인증 제출이 완료되었습니다."));
  }

  @GetMapping("/{challengeId}/verification/result")
  @Operation(summary = "단체 챌린지 인증 결과 조회", description = "최근 제출한 단체 챌린지 인증의 결과를 조회합니다.")
  public ResponseEntity<ApiResponse<Map<String, String>>> getVerificationResult(
      @CurrentMemberId Long memberId, 
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId) {

    ChallengeStatus status = resultQueryService.getLatestStatus(memberId, challengeId);

    Map<String, String> data = Map.of("status", status.name());
    String message = ChallengeStatusMessageResolver.resolveMessage(status);

    HttpStatus httpStatus =
        (status == ChallengeStatus.PENDING_APPROVAL) ? HttpStatus.ACCEPTED : HttpStatus.OK;

    return ResponseEntity.status(httpStatus).body(ApiResponse.success(message, data));
  }
}
