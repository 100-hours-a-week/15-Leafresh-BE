package ktb.leafresh.backend.domain.member.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import ktb.leafresh.backend.domain.challenge.group.application.service.GroupChallengeCreatedReadService;
import ktb.leafresh.backend.domain.challenge.group.application.service.GroupChallengeParticipationReadService;
import ktb.leafresh.backend.domain.challenge.group.application.service.GroupChallengeVerificationHistoryService;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeListResponseDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeParticipationCountResponseDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeParticipationListResponseDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeVerificationHistoryResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/challenges/group")
public class GroupChallengeMemberController {

    private final GroupChallengeParticipationReadService groupChallengeParticipationReadService;
    private final GroupChallengeVerificationHistoryService groupChallengeVerificationHistoryService;
    private final GroupChallengeCreatedReadService groupChallengeCreatedReadService;

    @GetMapping("/creations")
    @Operation(summary = "생성한 단체 챌린지 목록 조회", description = "회원이 생성한 단체 챌린지를 커서 기반으로 조회합니다.")
    public ResponseEntity<ApiResponse<GroupChallengeListResponseDto>> getCreatedChallenges(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "12") int size
    ) {
        Long memberId = userDetails.getMemberId();
        GroupChallengeListResponseDto response =
                groupChallengeCreatedReadService.getCreatedChallengesByMember(memberId, cursorId, size);

        return ResponseEntity.ok(ApiResponse.success("생성한 단체 챌린지 목록 조회에 성공했습니다.", response));
    }

    @GetMapping("/participations/count")
    public ResponseEntity<ApiResponse<GroupChallengeParticipationCountResponseDto>> getParticipationCounts(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        GroupChallengeParticipationCountResponseDto response =
                groupChallengeParticipationReadService.getParticipationCounts(userDetails.getMemberId());

        return ResponseEntity.ok(ApiResponse.success("참여한 단체 챌린지 카운트를 성공적으로 조회했습니다.", response));
    }

    @GetMapping("/participations")
    @Operation(summary = "참여한 단체 챌린지 목록 조회", description = "회원이 참여한 단체 챌린지를 status별로 커서 기반으로 조회합니다.")
    public ResponseEntity<ApiResponse<GroupChallengeParticipationListResponseDto>> getParticipatedChallenges(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String status,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "12") int size
    ) {
        GroupChallengeParticipationListResponseDto response =
                groupChallengeParticipationReadService.getParticipatedChallenges(userDetails.getMemberId(), status, cursorId, size);

        return ResponseEntity.ok(ApiResponse.success("참여한 단체 챌린지 목록을 성공적으로 조회했습니다.", response));
    }

    @GetMapping("/participations/{challengeId}/verifications")
    @Operation(summary = "참여 챌린지 인증내역 일별 조회", description = "참여한 챌린지의 인증내역을 일자별로 제공합니다.")
    public ResponseEntity<ApiResponse<GroupChallengeVerificationHistoryResponseDto>> getVerificationHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long challengeId
    ) {
        GroupChallengeVerificationHistoryResponseDto response =
                groupChallengeVerificationHistoryService.getVerificationHistory(userDetails.getMemberId(), challengeId);
        return ResponseEntity.ok(ApiResponse.success("단체 챌린지 인증 내역을 성공적으로 조회했습니다.", response));
    }
}
