package ktb.leafresh.backend.domain.verification.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.verification.application.service.GroupChallengeVerificationResultSaveService;
import ktb.leafresh.backend.domain.verification.application.service.PersonalChallengeVerificationResultSaveService;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.VerificationResultRequestDto;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeType;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Verification Result", description = "인증 결과 수신 API (외부 AI 서비스 전용)")
@RestController
@RequestMapping("/api/verifications")
@RequiredArgsConstructor
@Validated
public class VerificationResultController {

  private final PersonalChallengeVerificationResultSaveService
      personalChallengeVerificationResultSaveService;
  private final GroupChallengeVerificationResultSaveService
      groupChallengeVerificationResultSaveService;

  @PostMapping("/{verificationId}/result")
  @Operation(summary = "인증 결과 수신", description = "외부 AI 서비스로부터 인증 결과를 수신합니다. (외부 서비스 전용)")
  public ResponseEntity<ApiResponse<Void>> receiveResult(
      @Parameter(description = "인증 ID") @PathVariable Long verificationId, 
      @Valid @RequestBody VerificationResultRequestDto dto) {

    if (dto.type() == ChallengeType.GROUP) {
      groupChallengeVerificationResultSaveService.saveResult(verificationId, dto);
    } else {
      personalChallengeVerificationResultSaveService.saveResult(verificationId, dto);
    }

    return ResponseEntity.ok(ApiResponse.success("인증 결과 수신 완료"));
  }
}
