package ktb.leafresh.backend.domain.member.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import ktb.leafresh.backend.domain.challenge.group.application.service.GroupChallengeReadService;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeListResponseDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeParticipationCountResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/challenges/group")
public class GroupChallengeMemberController {

    private final GroupChallengeReadService groupChallengeReadService;

    @GetMapping("/creations")
    @Operation(summary = "생성한 단체 챌린지 목록 조회", description = "회원이 생성한 단체 챌린지를 커서 기반으로 조회합니다.")
    public ResponseEntity<ApiResponse<GroupChallengeListResponseDto>> getCreatedChallenges(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "12") int size
    ) {
        Long memberId = userDetails.getMemberId();
        GroupChallengeListResponseDto response =
                groupChallengeReadService.getCreatedChallengesByMember(memberId, cursorId, size);

        return ResponseEntity.ok(ApiResponse.success("생성한 단체 챌린지 목록 조회에 성공했습니다.", response));
    }

    @GetMapping("/participations/count")
    public ResponseEntity<ApiResponse<GroupChallengeParticipationCountResponseDto>> getParticipationCounts(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        GroupChallengeParticipationCountResponseDto response =
                groupChallengeReadService.getParticipationCounts(userDetails.getMemberId());

        return ResponseEntity.ok(ApiResponse.success("참여한 단체 챌린지 카운트를 성공적으로 조회했습니다.", response));
    }
}
