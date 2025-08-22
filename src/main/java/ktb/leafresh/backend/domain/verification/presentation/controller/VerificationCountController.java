package ktb.leafresh.backend.domain.verification.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ktb.leafresh.backend.domain.verification.application.service.VerificationCountReadService;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.VerificationCountResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Verification Count", description = "인증 수 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/verifications")
@Validated
public class VerificationCountController {

  private final VerificationCountReadService verificationCountReadService;

  @GetMapping("/count")
  @Operation(summary = "전체 인증 수 조회", description = "플랫폼 전체의 누적 인증 수를 조회합니다.")
  public ResponseEntity<ApiResponse<VerificationCountResponseDto>> getTotalVerificationCount() {

    VerificationCountResponseDto result = verificationCountReadService.getTotalVerificationCount();
    
    return ResponseEntity.ok(ApiResponse.success("누적 사용자 인증 수 조회에 성공했습니다.", result));
  }
}
