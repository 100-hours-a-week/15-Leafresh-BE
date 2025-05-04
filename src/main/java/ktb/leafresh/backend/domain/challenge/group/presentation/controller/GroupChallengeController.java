package ktb.leafresh.backend.domain.challenge.group.presentation.controller;

import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.challenge.group.application.service.GroupChallengeCreateService;
import ktb.leafresh.backend.domain.challenge.group.application.service.GroupChallengeReadService;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeCreateRequestDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeCreateResponseDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeDetailResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/group")
public class GroupChallengeController {

    private final GroupChallengeCreateService groupChallengeCreateService;
    private final GroupChallengeReadService groupChallengeReadService;

    @PostMapping
    public ResponseEntity<ApiResponse<GroupChallengeCreateResponseDto>> createGroupChallenge(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody GroupChallengeCreateRequestDto request
    ) {
        Long memberId = userDetails.getMemberId();
        GroupChallengeCreateResponseDto response = groupChallengeCreateService.create(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("단체 챌린지가 생성되었습니다.", response));
    }

    @GetMapping("/{challengeId}")
    public ResponseEntity<ApiResponse<GroupChallengeDetailResponseDto>> getGroupChallengeDetail(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = (userDetails != null) ? userDetails.getMemberId() : null;
        GroupChallengeDetailResponseDto response = groupChallengeReadService.getChallengeDetail(memberId, challengeId);
        return ResponseEntity.ok(ApiResponse.success("단체 챌린지 상세 정보를 성공적으로 조회했습니다.", response));
    }
}
