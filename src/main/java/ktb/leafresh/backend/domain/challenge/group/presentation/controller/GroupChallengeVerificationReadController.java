package ktb.leafresh.backend.domain.challenge.group.presentation.controller;

import ktb.leafresh.backend.domain.challenge.group.application.service.*;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.*;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/group/{challengeId}")
public class GroupChallengeVerificationReadController {

    private final GroupChallengeVerificationReadService groupChallengeVerificationReadService;

    @GetMapping("/verifications")
    public ResponseEntity<ApiResponse<GroupChallengeVerificationListResponseDto>> getVerifications(
            @PathVariable Long challengeId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "12") int size
    ) {
        GroupChallengeVerificationListResponseDto response = groupChallengeVerificationReadService
                .getVerifications(challengeId, cursorId, size);

        return ResponseEntity.ok(ApiResponse.success("단체 챌린지 인증 내역 조회에 성공했습니다.", response));
    }

    @GetMapping("/rules")
    public ResponseEntity<ApiResponse<GroupChallengeRuleResponseDto>> getGroupChallengeRules(
            @PathVariable Long challengeId
    ) {
        GroupChallengeRuleResponseDto response = groupChallengeVerificationReadService.getChallengeRules(challengeId);
        return ResponseEntity.ok(ApiResponse.success("단체 챌린지 인증 규약 정보를 성공적으로 조회했습니다.", response));
    }
}
