package ktb.leafresh.backend.domain.challenge.group.presentation.controller;

import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.challenge.group.application.service.GroupChallengeCreateService;
import ktb.leafresh.backend.domain.challenge.group.application.service.GroupChallengeDeleteService;
import ktb.leafresh.backend.domain.challenge.group.application.service.GroupChallengeReadService;
import ktb.leafresh.backend.domain.challenge.group.application.service.GroupChallengeUpdateService;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeCreateRequestDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeUpdateRequestDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeCreateResponseDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeDetailResponseDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeListResponseDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeVerificationListResponseDto;
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
@RequestMapping("/api/challenges/group")
public class GroupChallengeController {

    private final GroupChallengeCreateService groupChallengeCreateService;
    private final GroupChallengeReadService groupChallengeReadService;
    private final GroupChallengeUpdateService groupChallengeUpdateService;
    private final GroupChallengeDeleteService groupChallengeDeleteService;

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<ApiResponse<GroupChallengeListResponseDto>> getGroupChallengesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(required = false) String input,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "12") int size
    ) {
        GroupChallengeListResponseDto response = groupChallengeReadService
                .getGroupChallengesByCategory(categoryId, input, cursorId, size);

        return ResponseEntity.ok(ApiResponse.success("단체 챌린지 목록 조회에 성공하였습니다.", response));
    }

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

    @PatchMapping("/{challengeId}")
    public ResponseEntity<ApiResponse<Void>> updateGroupChallenge(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long challengeId,
            @Valid @RequestBody GroupChallengeUpdateRequestDto request
    ) {
        groupChallengeUpdateService.update(userDetails.getMemberId(), challengeId, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{challengeId}")
    public ResponseEntity<ApiResponse<Map<String, Long>>> deleteGroupChallenge(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long challengeId
    ) {
        Long memberId = userDetails.getMemberId();
        Long deletedId = groupChallengeDeleteService.delete(memberId, challengeId);
        return ResponseEntity.ok(ApiResponse.success("단체 챌린지가 성공적으로 삭제되었습니다.",
                Map.of("deletedChallengeId", deletedId)));
    }

    @GetMapping("/{challengeId}/verifications")
    public ResponseEntity<ApiResponse<GroupChallengeVerificationListResponseDto>> getVerifications(
            @PathVariable Long challengeId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "12") int size
    ) {
        GroupChallengeVerificationListResponseDto response = groupChallengeReadService
                .getVerifications(challengeId, cursorId, size);

        return ResponseEntity.ok(ApiResponse.success("단체 챌린지 인증 내역 조회에 성공했습니다.", response));
    }
}
