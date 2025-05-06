package ktb.leafresh.backend.domain.verification.presentation.controller;

import ktb.leafresh.backend.domain.verification.application.service.GroupChallengeVerificationResultQueryService;
import ktb.leafresh.backend.domain.verification.application.service.GroupChallengeVerificationSubmitService;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.GroupChallengeVerificationRequestDto;
import ktb.leafresh.backend.domain.verification.presentation.util.ChallengeStatusMessageResolver;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static ktb.leafresh.backend.global.exception.GlobalErrorCode.UNAUTHORIZED;

@Slf4j
@RestController
@RequestMapping("/api/challenges/group")
@RequiredArgsConstructor
public class GroupChallengeVerificationController {

    private final GroupChallengeVerificationSubmitService submitService;
    private final GroupChallengeVerificationResultQueryService resultQueryService;

    @PostMapping("/{challengeId}/verifications")
    public ResponseEntity<ApiResponse<Void>> submitVerification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long challengeId,
            @RequestBody GroupChallengeVerificationRequestDto requestDto
    ) {
        log.info("[단체 인증 제출 요청] challengeId={}, imageUrl={}, content={}",
                challengeId, requestDto.imageUrl(), requestDto.content());

        if (userDetails == null) {
            log.warn("[단체 인증 제출 실패] 인증 정보가 없습니다.");
            throw new IllegalStateException("로그인 정보가 존재하지 않습니다.");
        }

        Long memberId = userDetails.getMemberId();
        log.info("[단체 인증 제출] memberId={}", memberId);

        submitService.submit(memberId, challengeId, requestDto);
        log.info("[단체 인증 제출 완료] challengeId={}, memberId={}", challengeId, memberId);

        return ResponseEntity.ok(ApiResponse.success("단체 챌린지 인증 제출이 완료되었습니다."));
    }

    @GetMapping("/{challengeId}/verification/result")
    public ResponseEntity<ApiResponse<Map<String, String>>> getVerificationResult(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long challengeId
    ) {
        if (userDetails == null) {
            return ResponseEntity
                    .status(UNAUTHORIZED.getStatus())
                    .body(ApiResponse.error(UNAUTHORIZED.getStatus(), UNAUTHORIZED.getMessage()));
        }

        Long memberId = userDetails.getMemberId();
        ChallengeStatus status = resultQueryService.waitForResult(memberId, challengeId);

        Map<String, String> data = Map.of("status", status.name());
        String message = ChallengeStatusMessageResolver.resolveMessage(status);

        HttpStatus httpStatus = (status == ChallengeStatus.PENDING_APPROVAL)
                ? HttpStatus.ACCEPTED : HttpStatus.OK;

        return ResponseEntity.status(httpStatus).body(ApiResponse.success(message, data));
    }
}
