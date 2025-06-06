package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.domain.entity.Comment;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.CommentRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import ktb.leafresh.backend.global.util.redis.VerificationStatRedisLuaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupVerificationCommentDeleteService {

    private final GroupChallengeVerificationRepository verificationRepository;
    private final CommentRepository commentRepository;
    private final VerificationStatRedisLuaService verificationStatRedisLuaService;

    @Transactional
    public void deleteComment(Long challengeId, Long verificationId, Long commentId, Long memberId) {
        try {
            log.info("[댓글 삭제 요청] challengeId={}, verificationId={}, commentId={}, memberId={}",
                    challengeId, verificationId, commentId, memberId);

            GroupChallengeVerification verification = verificationRepository.findByIdAndDeletedAtIsNull(verificationId)
                    .orElseThrow(() -> new CustomException(VerificationErrorCode.VERIFICATION_DETAIL_NOT_FOUND));

            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new CustomException(VerificationErrorCode.COMMENT_NOT_FOUND));

            if (!comment.getMember().getId().equals(memberId)) {
                throw new CustomException(GlobalErrorCode.ACCESS_DENIED);
            }

            if (comment.isDeleted()) {
                throw new CustomException(VerificationErrorCode.CANNOT_EDIT_DELETED_COMMENT);
            }

            comment.softDelete();
            verificationStatRedisLuaService.decreaseVerificationCommentCount(verificationId);

            log.info("[댓글 삭제 완료] commentId={}, memberId={}", commentId, memberId);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[댓글 삭제 실패] challengeId={}, verificationId={}, commentId={}, memberId={}, error={}",
                    challengeId, verificationId, commentId, memberId, e.getMessage(), e);
            throw new CustomException(VerificationErrorCode.COMMENT_UPDATE_FAILED); // 삭제 실패 코드 새로 정의 가능
        }
    }
}
