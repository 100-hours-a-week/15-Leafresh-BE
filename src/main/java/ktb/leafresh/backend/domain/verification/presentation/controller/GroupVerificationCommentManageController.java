package ktb.leafresh.backend.domain.verification.presentation.controller;

import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.verification.application.service.GroupVerificationCommentCreateService;
import ktb.leafresh.backend.domain.verification.application.service.GroupVerificationCommentDeleteService;
import ktb.leafresh.backend.domain.verification.application.service.GroupVerificationCommentQueryService;
import ktb.leafresh.backend.domain.verification.application.service.GroupVerificationCommentUpdateService;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.GroupVerificationCommentCreateRequestDto;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.CommentListResponseDto;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.CommentSummaryResponseDto;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.CommentResponseDto;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.CommentUpdateResponseDto;
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

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/group/{challengeId}/verifications/{verificationId}/comments")
public class GroupVerificationCommentManageController {

  private final GroupVerificationCommentCreateService groupVerificationCommentCreateService;
  private final GroupVerificationCommentUpdateService groupVerificationCommentUpdateService;
  private final GroupVerificationCommentDeleteService groupVerificationCommentDeleteService;
  private final GroupVerificationCommentQueryService groupVerificationCommentQueryService;

  @GetMapping
  public ResponseEntity<ApiResponse<CommentListResponseDto>> getComments(
      @PathVariable Long challengeId,
      @PathVariable Long verificationId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Long memberId = userDetails != null ? userDetails.getMemberId() : null;

    try {
      List<CommentSummaryResponseDto> comments =
          groupVerificationCommentQueryService.getComments(challengeId, verificationId, memberId);
      CommentListResponseDto responseDto = new CommentListResponseDto(comments);

      return ResponseEntity.ok(ApiResponse.success("댓글 목록을 조회했습니다.", responseDto));
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error(
          "[댓글 목록 조회 실패] challengeId={}, verificationId={}, memberId={}, error={}",
          challengeId,
          verificationId,
          memberId,
          e.getMessage(),
          e);
      throw new CustomException(VerificationErrorCode.VERIFICATION_LIST_QUERY_FAILED);
    }
  }

  @PostMapping
  public ResponseEntity<ApiResponse<CommentResponseDto>> createComment(
      @PathVariable Long challengeId,
      @PathVariable Long verificationId,
      @Valid @RequestBody GroupVerificationCommentCreateRequestDto requestDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    if (userDetails == null) {
      throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
    }

    Long memberId = userDetails.getMemberId();

    try {
      CommentResponseDto response =
          groupVerificationCommentCreateService.createComment(
              challengeId, verificationId, memberId, requestDto);

      return ResponseEntity.ok(ApiResponse.success("댓글이 작성되었습니다.", response));

    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error(
          "[댓글 생성 실패] challengeId={}, verificationId={}, memberId={}, error={}",
          challengeId,
          verificationId,
          memberId,
          e.getMessage(),
          e);
      throw new CustomException(VerificationErrorCode.COMMENT_CREATE_FAILED);
    }
  }

  @PostMapping("/{commentId}/replies")
  public ResponseEntity<ApiResponse<CommentResponseDto>> createReply(
      @PathVariable Long challengeId,
      @PathVariable Long verificationId,
      @PathVariable Long commentId,
      @Valid @RequestBody GroupVerificationCommentCreateRequestDto requestDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    if (userDetails == null) {
      throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
    }

    Long memberId = userDetails.getMemberId();

    try {
      CommentResponseDto response =
          groupVerificationCommentCreateService.createReply(
              challengeId, verificationId, commentId, memberId, requestDto);

      return ResponseEntity.ok(ApiResponse.success("대댓글이 작성되었습니다.", response));

    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error(
          "[대댓글 생성 실패] challengeId={}, verificationId={}, commentId={}, memberId={}, error={}",
          challengeId,
          verificationId,
          commentId,
          memberId,
          e.getMessage(),
          e);
      throw new CustomException(VerificationErrorCode.COMMENT_CREATE_FAILED);
    }
  }

  @PutMapping("/{commentId}")
  public ResponseEntity<ApiResponse<CommentUpdateResponseDto>> updateComment(
      @PathVariable Long challengeId,
      @PathVariable Long verificationId,
      @PathVariable Long commentId,
      @Valid @RequestBody GroupVerificationCommentCreateRequestDto requestDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    if (userDetails == null) {
      throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
    }

    Long memberId = userDetails.getMemberId();

    try {
      CommentUpdateResponseDto response =
          groupVerificationCommentUpdateService.updateComment(
              challengeId, verificationId, commentId, memberId, requestDto);
      return ResponseEntity.ok(ApiResponse.success("댓글이 수정되었습니다.", response));
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error(
          "[댓글 수정 실패] challengeId={}, verificationId={}, commentId={}, memberId={}, error={}",
          challengeId,
          verificationId,
          commentId,
          memberId,
          e.getMessage(),
          e);
      throw new CustomException(VerificationErrorCode.COMMENT_UPDATE_FAILED);
    }
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<ApiResponse<Void>> deleteComment(
      @PathVariable Long challengeId,
      @PathVariable Long verificationId,
      @PathVariable Long commentId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    if (userDetails == null) {
      throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
    }

    Long memberId = userDetails.getMemberId();

    try {
      groupVerificationCommentDeleteService.deleteComment(
          challengeId, verificationId, commentId, memberId);
      return ResponseEntity.ok(ApiResponse.success("댓글이 삭제되었습니다."));
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error(
          "[댓글 삭제 실패] challengeId={}, verificationId={}, commentId={}, memberId={}, error={}",
          challengeId,
          verificationId,
          commentId,
          memberId,
          e.getMessage(),
          e);
      throw new CustomException(VerificationErrorCode.COMMENT_UPDATE_FAILED);
    }
  }
}
