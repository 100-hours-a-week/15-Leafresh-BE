package ktb.leafresh.backend.domain.verification.presentation.controller;

import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.verification.application.service.GroupVerificationCommentCreateService;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.GroupVerificationCommentCreateRequestDto;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.CommentResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/group/{challengeId}/verifications/{verificationId}/comments")
public class GroupVerificationCommentCreateController {

    private final GroupVerificationCommentCreateService groupVerificationCommentCreateService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponseDto>> createComment(
            @PathVariable Long challengeId,
            @PathVariable Long verificationId,
            @Valid @RequestBody GroupVerificationCommentCreateRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
        }

        Long memberId = userDetails.getMemberId();

        try {
            CommentResponseDto response = groupVerificationCommentCreateService.createComment(challengeId, verificationId, memberId, requestDto);

            return ResponseEntity.ok(ApiResponse.success("댓글이 작성되었습니다.", response));

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[댓글 생성 실패] challengeId={}, verificationId={}, memberId={}, error={}",
                    challengeId, verificationId, memberId, e.getMessage(), e);
            throw new CustomException(VerificationErrorCode.COMMENT_CREATE_FAILED);
        }
    }
}
