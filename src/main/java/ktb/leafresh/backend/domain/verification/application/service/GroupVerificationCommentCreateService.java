package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.verification.domain.entity.Comment;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.CommentRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.GroupVerificationCommentCreateRequestDto;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.CommentResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import ktb.leafresh.backend.global.util.redis.VerificationStatRedisLuaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupVerificationCommentCreateService {

    private final CommentRepository commentRepository;
    private final GroupChallengeVerificationRepository verificationRepository;
    private final MemberRepository memberRepository;
    private final VerificationStatRedisLuaService verificationStatRedisLuaService;

    @Transactional
    public CommentResponseDto createComment(Long challengeId, Long verificationId, Long memberId, GroupVerificationCommentCreateRequestDto dto) {
        try {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

            GroupChallengeVerification verification = verificationRepository.findByIdAndDeletedAtIsNull(verificationId)
                    .orElseThrow(() -> new CustomException(VerificationErrorCode.VERIFICATION_DETAIL_NOT_FOUND));

            Comment comment = Comment.builder()
                    .verification(verification)
                    .member(member)
                    .content(dto.content())
                    .build();

            commentRepository.save(comment);
            verificationStatRedisLuaService.increaseVerificationCommentCount(verificationId);

            log.info("[댓글 생성 완료] verificationId={}, commentId={}, memberId={}",
                    verificationId, comment.getId(), memberId);

            return CommentResponseDto.from(comment);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[댓글 생성 실패] challengeId={}, verificationId={}, memberId={}, error={}",
                    challengeId, verificationId, memberId, e.getMessage(), e);
            throw new CustomException(VerificationErrorCode.COMMENT_CREATE_FAILED);
        }
    }

    @Transactional
    public CommentResponseDto createReply(Long challengeId, Long verificationId, Long parentCommentId, Long memberId, GroupVerificationCommentCreateRequestDto dto) {
        try {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

            GroupChallengeVerification verification = verificationRepository.findByIdAndDeletedAtIsNull(verificationId)
                    .orElseThrow(() -> new CustomException(VerificationErrorCode.VERIFICATION_DETAIL_NOT_FOUND));

            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new CustomException(VerificationErrorCode.COMMENT_NOT_FOUND));

            if (parentComment.getDeletedAt() != null) {
                throw new CustomException(VerificationErrorCode.CANNOT_REPLY_TO_DELETED_COMMENT);
            }

            Comment reply = Comment.builder()
                    .verification(verification)
                    .member(member)
                    .content(dto.content())
                    .parentComment(parentComment)
                    .build();

            commentRepository.save(reply);
            verificationStatRedisLuaService.increaseVerificationCommentCount(verificationId);

            log.info("[대댓글 생성 완료] verificationId={}, parentCommentId={}, replyId={}, memberId={}",
                    verificationId, parentCommentId, reply.getId(), memberId);

            return CommentResponseDto.from(reply);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[대댓글 생성 실패] challengeId={}, verificationId={}, parentCommentId={}, memberId={}, error={}",
                    challengeId, verificationId, parentCommentId, memberId, e.getMessage(), e);
            throw new CustomException(VerificationErrorCode.COMMENT_CREATE_FAILED);
        }
    }
}
