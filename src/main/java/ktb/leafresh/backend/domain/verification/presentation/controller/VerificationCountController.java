package ktb.leafresh.backend.domain.verification.presentation.controller;

import ktb.leafresh.backend.domain.verification.application.service.VerificationCountReadService;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.VerificationCountResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/verifications")
public class VerificationCountController {

    private final VerificationCountReadService verificationCountReadService;

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<VerificationCountResponseDto>> getTotalVerificationCount() {
        try {
            VerificationCountResponseDto result = verificationCountReadService.getTotalVerificationCount();
            log.info("[VerificationCountController] 누적 인증 수 조회 성공: {}", result.count());
            return ResponseEntity.ok(ApiResponse.success("누적 사용자 인증 수 조회에 성공했습니다.", result));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[VerificationCountController] 누적 인증 수 조회 실패", e);
            throw new CustomException(VerificationErrorCode.VERIFICATION_COUNT_QUERY_FAILED);
        }
    }
}
