package ktb.leafresh.backend.domain.verification.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Group Verification Comment", description = "단체 챌린지 인증 댓글 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/group/{challengeId}/verifications/{verificationId}/comments")
@Validated
public class GroupVerificationCommentManageController {

  private final GroupVerificationCommentCreateService groupVerificationCommentCreateService;
  private final GroupVerificationCommentUpdateService groupVerificationCommentUpdateService;
  private final GroupVerificationCommentDeleteService groupVerificationCommentDeleteService;
  private final GroupVerificationCommentQueryService groupVerificationCommentQueryService;

  @GetMapping
  @Operation(summary = "댓글 목록 조회", description = "단체 챌린지 인증의 댓글 목록을 조회합니다.")
  public ResponseEntity<ApiResponse<CommentListResponseDto>> getComments(
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
      @Parameter(description = "인증 ID") @PathVariable Long verificationId,
      @CurrentMemberId Long memberId) {

    List<CommentSummaryResponseDto> comments =
        groupVerificationCommentQueryService.getComments(challengeId, verificationId, memberId);

    CommentListResponseDto responseDto = new CommentListResponseDto(comments);

    return ResponseEntity.ok(ApiResponse.success("댓글 목록을 조회했습니다.", responseDto));
  }

  @PostMapping
  @Operation(summary = "댓글 작성", description = "단체 챌린지 인증에 댓글을 작성합니다.")
  public ResponseEntity<ApiResponse<CommentResponseDto>> createComment(
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
      @Parameter(description = "인증 ID") @PathVariable Long verificationId,
      @Valid @RequestBody GroupVerificationCommentCreateRequestDto requestDto,
      @CurrentMemberId Long memberId) {

    CommentResponseDto response =
        groupVerificationCommentCreateService.createComment(
            challengeId, verificationId, memberId, requestDto);

    return ResponseEntity.ok(ApiResponse.success("댓글이 작성되었습니다.", response));
  }

  @PostMapping("/{commentId}/replies")
  @Operation(summary = "대댓글 작성", description = "댓글에 대한 대댓글을 작성합니다.")
  public ResponseEntity<ApiResponse<CommentResponseDto>> createReply(
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
      @Parameter(description = "인증 ID") @PathVariable Long verificationId,
      @Parameter(description = "댓글 ID") @PathVariable Long commentId,
      @Valid @RequestBody GroupVerificationCommentCreateRequestDto requestDto,
      @CurrentMemberId Long memberId) {

    CommentResponseDto response =
        groupVerificationCommentCreateService.createReply(
            challengeId, verificationId, commentId, memberId, requestDto);

    return ResponseEntity.ok(ApiResponse.success("대댓글이 작성되었습니다.", response));
  }

  @PutMapping("/{commentId}")
  @Operation(summary = "댓글 수정", description = "작성한 댓글을 수정합니다.")
  public ResponseEntity<ApiResponse<CommentUpdateResponseDto>> updateComment(
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
      @Parameter(description = "인증 ID") @PathVariable Long verificationId,
      @Parameter(description = "댓글 ID") @PathVariable Long commentId,
      @Valid @RequestBody GroupVerificationCommentCreateRequestDto requestDto,
      @CurrentMemberId Long memberId) {

    CommentUpdateResponseDto response =
        groupVerificationCommentUpdateService.updateComment(
            challengeId, verificationId, commentId, memberId, requestDto);

    return ResponseEntity.ok(ApiResponse.success("댓글이 수정되었습니다.", response));
  }

  @DeleteMapping("/{commentId}")
  @Operation(summary = "댓글 삭제", description = "작성한 댓글을 삭제합니다.")
  public ResponseEntity<ApiResponse<Void>> deleteComment(
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
      @Parameter(description = "인증 ID") @PathVariable Long verificationId,
      @Parameter(description = "댓글 ID") @PathVariable Long commentId,
      @CurrentMemberId Long memberId) {

    groupVerificationCommentDeleteService.deleteComment(
        challengeId, verificationId, commentId, memberId);

    return ResponseEntity.ok(ApiResponse.success("댓글이 삭제되었습니다."));
  }
}
