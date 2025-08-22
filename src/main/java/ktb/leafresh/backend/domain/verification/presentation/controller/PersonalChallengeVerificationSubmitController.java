package ktb.leafresh.backend.domain.verification.presentation.controller;

import ktb.leafresh.backend.domain.verification.application.service.PersonalChallengeVerificationResultQueryService;
import ktb.leafresh.backend.domain.verification.application.service.PersonalChallengeVerificationSubmitService;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.PersonalChallengeVerificationRequestDto;
import ktb.leafresh.backend.domain.verification.presentation.util.ChallengeStatusMessageResolver;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/challenges/personal")
@RequiredArgsConstructor
public class PersonalChallengeVerificationSubmitController {

  private final PersonalChallengeVerificationSubmitService submitService;
  private final PersonalChallengeVerificationResultQueryService resultQueryService;

  @PostMapping("/{challengeId}/verifications")
  public ResponseEntity<ApiResponse<Void>> submitVerification(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long challengeId,
      @RequestBody PersonalChallengeVerificationRequestDto requestDto) {
    log.info(
        "[인증 제출 요청] challengeId={}, imageUrl={}, content={}",
        challengeId,
        requestDto.imageUrl(),
        requestDto.content());

    if (userDetails == null) {
      throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
    }

    Long memberId = userDetails.getMemberId();
    log.info("[인증 제출] memberId={}", memberId);

    submitService.submit(memberId, challengeId, requestDto);
    log.info("[인증 제출 완료] challengeId={}, memberId={}", challengeId, memberId);

    return ResponseEntity.ok(ApiResponse.success("인증 제출이 완료되었습니다."));
  }

  @GetMapping("/{challengeId}/verification/result")
  public ResponseEntity<ApiResponse<Map<String, String>>> getVerificationResult(
      @AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long challengeId) {
    if (userDetails == null) {
      throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
    }

    Long memberId = userDetails.getMemberId();
    ChallengeStatus status = resultQueryService.getLatestStatus(memberId, challengeId);

    Map<String, String> data = Map.of("status", status.name());
    String message = ChallengeStatusMessageResolver.resolveMessage(status);

    HttpStatus httpStatus =
        (status == ChallengeStatus.PENDING_APPROVAL) ? HttpStatus.ACCEPTED : HttpStatus.OK;

    return ResponseEntity.status(httpStatus).body(ApiResponse.success(message, data));
  }
}
