package ktb.leafresh.backend.domain.verification.presentation.controller;

import ktb.leafresh.backend.domain.verification.application.service.PersonalChallengeVerificationSubmitService;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.PersonalChallengeVerificationRequestDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/challenges/personal")
@RequiredArgsConstructor
public class PersonalChallengeVerificationController {

    private final PersonalChallengeVerificationSubmitService submitService;

    @PostMapping("/{challengeId}/verifications")
    public ResponseEntity<ApiResponse<Void>> submitVerification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long challengeId,
            @RequestBody PersonalChallengeVerificationRequestDto requestDto
    ) {
        log.info("[인증 제출 요청] challengeId={}, imageUrl={}, content={}",
                challengeId, requestDto.imageUrl(), requestDto.content());

        if (userDetails == null) {
            log.warn("[인증 제출 실패] 인증 정보가 없습니다.");
            throw new IllegalStateException("로그인 정보가 존재하지 않습니다.");
        }

        Long memberId = userDetails.getMemberId();
        log.info("[인증 제출] memberId={}", memberId);

        submitService.submit(memberId, challengeId, requestDto);
        log.info("[인증 제출 완료] challengeId={}, memberId={}", challengeId, memberId);

        return ResponseEntity.ok(ApiResponse.success("인증 제출이 완료되었습니다."));
    }
}
