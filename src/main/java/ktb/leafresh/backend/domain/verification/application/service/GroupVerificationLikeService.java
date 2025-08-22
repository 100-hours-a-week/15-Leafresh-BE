package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.domain.entity.Like;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.LikeRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import ktb.leafresh.backend.global.util.redis.VerificationStatRedisLuaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupVerificationLikeService {

  private final GroupChallengeVerificationRepository verificationRepository;
  private final LikeRepository likeRepository;
  private final MemberRepository memberRepository;
  private final VerificationStatRedisLuaService verificationStatRedisLuaService;

  @Transactional
  public boolean likeVerification(Long verificationId, Long memberId) {
    GroupChallengeVerification verification =
        verificationRepository
            .findByIdAndDeletedAtIsNull(verificationId)
            .orElseThrow(
                () -> new CustomException(VerificationErrorCode.VERIFICATION_DETAIL_NOT_FOUND));

    Member member =
        memberRepository
            .findByIdAndDeletedAtIsNull(memberId)
            .orElseThrow(() -> new CustomException(GlobalErrorCode.UNAUTHORIZED));

    // 1. 삭제되지 않은 좋아요가 이미 있는 경우 → true 반환
    if (likeRepository.existsByVerificationIdAndMemberIdAndDeletedAtIsNull(
        verificationId, memberId)) {
      return true;
    }

    // 2. soft delete된 좋아요가 있는 경우 → 복구
    Like like =
        likeRepository.findByVerificationIdAndMemberId(verificationId, memberId).orElse(null);

    if (like != null && like.isDeleted()) {
      like.restoreLike();
      verificationStatRedisLuaService.increaseVerificationLikeCount(verificationId);
      return true;
    }

    likeRepository.save(Like.builder().verification(verification).member(member).build());

    verificationStatRedisLuaService.increaseVerificationLikeCount(verificationId);
    return true;
  }

  @Transactional
  public boolean cancelLike(Long verificationId, Long memberId) {
    GroupChallengeVerification verification =
        verificationRepository
            .findByIdAndDeletedAtIsNull(verificationId)
            .orElseThrow(
                () -> new CustomException(VerificationErrorCode.VERIFICATION_DETAIL_NOT_FOUND));

    Member member =
        memberRepository
            .findByIdAndDeletedAtIsNull(memberId)
            .orElseThrow(() -> new CustomException(GlobalErrorCode.UNAUTHORIZED));

    Like like =
        likeRepository
            .findByVerificationIdAndMemberIdAndDeletedAtIsNull(verificationId, memberId)
            .orElse(null);

    if (like == null) {
      // 이미 취소된 상태여도 200 반환
      return false;
    }

    like.softDelete();
    verificationStatRedisLuaService.decreaseVerificationLikeCount(verificationId);
    return false;
  }
}
