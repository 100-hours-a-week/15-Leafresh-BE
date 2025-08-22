package ktb.leafresh.backend.domain.challenge.group.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import ktb.leafresh.backend.domain.challenge.group.application.service.*;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Group Challenge Participation", description = "단체 챌린지 참여 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/group/{challengeId}/participations")
public class GroupChallengeParticipationController {

  private final GroupChallengeParticipationService groupChallengeParticipationService;

  @PostMapping
  @Operation(summary = "단체 챌린지 참여", description = "특정 단체 챌린지에 참여합니다.")
  public ResponseEntity<ApiResponse<Map<String, Long>>> participateGroupChallenge(
      @CurrentMemberId Long memberId, 
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId) {
    
    Long recordId = groupChallengeParticipationService.participate(memberId, challengeId);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created("단체 챌린지에 참여하였습니다.", Map.of("id", recordId)));
  }

  @DeleteMapping
  @Operation(summary = "단체 챌린지 참여 취소", description = "단체 챌린지 참여를 취소합니다.")
  public ResponseEntity<ApiResponse<Void>> cancelParticipation(
      @CurrentMemberId Long memberId, 
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId) {
    
    groupChallengeParticipationService.drop(memberId, challengeId);
    return ResponseEntity.noContent().build();
  }
}
