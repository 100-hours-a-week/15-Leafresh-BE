package ktb.leafresh.backend.domain.verification.presentation.controller;

import ktb.leafresh.backend.domain.verification.application.service.GroupChallengeVerificationResultSaveService;
import ktb.leafresh.backend.domain.verification.application.service.PersonalChallengeVerificationResultSaveService;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.VerificationResultRequestDto;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeType;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/verifications")
@RequiredArgsConstructor
public class VerificationResultController {

  private final PersonalChallengeVerificationResultSaveService
      personalChallengeVerificationResultSaveService;
  private final GroupChallengeVerificationResultSaveService
      groupChallengeVerificationResultSaveService;

  @PostMapping("/{verificationId}/result")
  public ResponseEntity<ApiResponse<Void>> receiveResult(
      @PathVariable Long verificationId, @RequestBody @Validated VerificationResultRequestDto dto) {
    log.info("[인증 결과 수신 API 호출] verificationId={}, result={}", verificationId, dto.result());

    if (dto.type() == ChallengeType.GROUP) {
      groupChallengeVerificationResultSaveService.saveResult(verificationId, dto);
    } else {
      personalChallengeVerificationResultSaveService.saveResult(verificationId, dto);
    }

    return ResponseEntity.ok(ApiResponse.success("인증 결과 수신 완료"));
  }
}
