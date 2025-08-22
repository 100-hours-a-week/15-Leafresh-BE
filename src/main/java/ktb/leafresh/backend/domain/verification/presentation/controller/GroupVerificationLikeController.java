package ktb.leafresh.backend.domain.verification.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import ktb.leafresh.backend.domain.verification.application.service.GroupVerificationLikeService;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Group Verification Like", description = "단체 챌린지 인증 좋아요 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/group/{challengeId}/verifications/{verificationId}/likes")
@Validated
public class GroupVerificationLikeController {

  private final GroupVerificationLikeService groupVerificationLikeService;

  @PostMapping
  @Operation(summary = "인증 좋아요", description = "단체 챌린지 인증에 좋아요를 누릅니다.")
  public ResponseEntity<ApiResponse<Map<String, Boolean>>> likeVerification(
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
      @Parameter(description = "인증 ID") @PathVariable Long verificationId,
      @CurrentMemberId Long memberId) {

    boolean isLiked = groupVerificationLikeService.likeVerification(verificationId, memberId);
    return ResponseEntity.ok(ApiResponse.success("좋아요를 눌렀습니다.", Map.of("isLiked", isLiked)));
  }

  @DeleteMapping
  @Operation(summary = "인증 좋아요 취소", description = "단체 챌린지 인증의 좋아요를 취소합니다.")
  public ResponseEntity<ApiResponse<Map<String, Boolean>>> cancelLikeVerification(
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
      @Parameter(description = "인증 ID") @PathVariable Long verificationId,
      @CurrentMemberId Long memberId) {

    boolean isLiked = groupVerificationLikeService.cancelLike(verificationId, memberId);
    return ResponseEntity.ok(ApiResponse.success("좋아요를 취소했습니다.", Map.of("isLiked", isLiked)));
  }
}
