package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.domain.entity.Comment;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.CommentRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.GroupVerificationCommentCreateRequestDto;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.CommentResponseDto;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.CommentUpdateResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupVerificationCommentUpdateService {

    private final GroupChallengeVerificationRepository groupChallengeVerificationRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public CommentUpdateResponseDto updateComment(Long challengeId, Long verificationId, Long commentId, Long memberId, GroupVerificationCommentCreateRequestDto dto) {
        try {
            log.info("[댓글 수정 요청] challengeId={}, verificationId={}, commentId={}, memberId={}", challengeId, verificationId, commentId, memberId);

            GroupChallengeVerification verification = groupChallengeVerificationRepository.findByIdAndDeletedAtIsNull(verificationId)
                    .orElseThrow(() -> new CustomException(VerificationErrorCode.VERIFICATION_DETAIL_NOT_FOUND));

            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new CustomException(VerificationErrorCode.COMMENT_NOT_FOUND));

            if (!comment.getMember().getId().equals(memberId)) {
                throw new CustomException(GlobalErrorCode.ACCESS_DENIED);
            }

            if (comment.getDeletedAt() != null) {
                throw new CustomException(VerificationErrorCode.CANNOT_EDIT_DELETED_COMMENT);
            }

            comment.updateContent(dto.content());
            log.info("[댓글 수정 완료] commentId={}, content={}, updatedAt={}", comment.getId(), comment.getContent(), comment.getUpdatedAt());

            List<CommentUpdateResponseDto> childDtos = commentRepository.findByParentCommentAndDeletedAtIsNull(comment).stream()
                    .map(child -> CommentUpdateResponseDto.from(child, List.of()))
                    .toList();

            return CommentUpdateResponseDto.from(comment, childDtos);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[댓글 수정 예외] challengeId={}, verificationId={}, commentId={}, memberId={}, error={}",
                    challengeId, verificationId, commentId, memberId, e.getMessage(), e);
            throw new CustomException(VerificationErrorCode.COMMENT_UPDATE_FAILED);
        }
    }
}
