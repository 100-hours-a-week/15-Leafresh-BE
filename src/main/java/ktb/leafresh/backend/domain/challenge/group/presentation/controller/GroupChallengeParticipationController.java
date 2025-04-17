package ktb.leafresh.backend.domain.challenge.group.presentation.controller;

import ktb.leafresh.backend.domain.challenge.group.application.service.*;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/group/{challengeId}/participations")
public class GroupChallengeParticipationController {

    private final GroupChallengeParticipationService groupChallengeParticipationService;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> participateGroupChallenge(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long challengeId
    ) {
        Long memberId = userDetails.getMemberId();
        Long recordId = groupChallengeParticipationService.participate(memberId, challengeId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("단체 챌린지에 참여하였습니다.", Map.of("id", recordId)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> cancelParticipation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long challengeId
    ) {
        groupChallengeParticipationService.drop(userDetails.getMemberId(), challengeId);
        return ResponseEntity.noContent().build();
    }
}
