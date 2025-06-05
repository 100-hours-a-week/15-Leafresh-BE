package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.verification.domain.entity.Comment;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.cache.VerificationStatCacheService;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.CommentRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.GroupVerificationCommentCreateRequestDto;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.CommentResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
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
    private final VerificationStatCacheService verificationStatCacheService;

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
            verificationStatCacheService.increaseCommentCount(verificationId);

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
}
