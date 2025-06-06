package ktb.leafresh.backend.domain.verification.presentation.controller;

import ktb.leafresh.backend.domain.verification.application.service.GroupVerificationLikeService;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/group/{challengeId}/verifications/{verificationId}/likes")
public class GroupVerificationLikeController {

    private final GroupVerificationLikeService groupVerificationLikeService;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> likeVerification(
            @PathVariable Long challengeId,
            @PathVariable Long verificationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
        }

        Long memberId = userDetails.getMemberId();

        try {
            boolean isLiked = groupVerificationLikeService.likeVerification(verificationId, memberId);
            return ResponseEntity.ok(ApiResponse.success("좋아요를 눌렀습니다.", Map.of("isLiked", isLiked)));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "좋아요 작업에 실패했습니다.");
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> cancelLikeVerification(
            @PathVariable Long challengeId,
            @PathVariable Long verificationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
        }

        Long memberId = userDetails.getMemberId();

        try {
            boolean isLiked = groupVerificationLikeService.cancelLike(verificationId, memberId);
            return ResponseEntity.ok(ApiResponse.success("좋아요를 취소했습니다.", Map.of("isLiked", isLiked)));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "좋아요 작업에 실패했습니다.");
        }
    }
}
