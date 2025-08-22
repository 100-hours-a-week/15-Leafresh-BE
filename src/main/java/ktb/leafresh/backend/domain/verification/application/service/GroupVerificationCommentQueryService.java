package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.domain.entity.Comment;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.CommentRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.presentation.assembler.CommentHierarchyBuilder;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.CommentSummaryResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupVerificationCommentQueryService {

  private final GroupChallengeVerificationRepository verificationRepository;
  private final CommentRepository commentRepository;

  @Transactional(readOnly = true)
  public List<CommentSummaryResponseDto> getComments(
      Long challengeId, Long verificationId, Long loginMemberId) {
    GroupChallengeVerification verification =
        verificationRepository
            .findByIdAndDeletedAtIsNull(verificationId)
            .orElseThrow(
                () -> new CustomException(VerificationErrorCode.VERIFICATION_DETAIL_NOT_FOUND));

    // 인증 ID 기반 댓글 + 작성자 모두 fetch
    List<Comment> comments = commentRepository.findAllByVerificationIdWithMember(verificationId);

    return CommentHierarchyBuilder.build(comments, loginMemberId);
  }
}
