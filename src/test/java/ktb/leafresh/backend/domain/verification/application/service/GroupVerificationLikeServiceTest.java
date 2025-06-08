package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.domain.entity.Like;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.LikeRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import ktb.leafresh.backend.global.util.redis.VerificationStatRedisLuaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static ktb.leafresh.backend.support.fixture.GroupChallengeVerificationFixture.of;
import static ktb.leafresh.backend.support.fixture.MemberFixture.of;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GroupVerificationLikeServiceTest {

    private GroupChallengeVerificationRepository verificationRepository;
    private LikeRepository likeRepository;
    private ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository memberRepository;
    private VerificationStatRedisLuaService redisService;
    private GroupVerificationLikeService service;

    private Member member;
    private GroupChallengeVerification verification;

    @BeforeEach
    void setUp() {
        verificationRepository = mock(GroupChallengeVerificationRepository.class);
        likeRepository = mock(LikeRepository.class);
        memberRepository = mock(ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository.class);
        redisService = mock(VerificationStatRedisLuaService.class);

        service = new GroupVerificationLikeService(
                verificationRepository, likeRepository, memberRepository, redisService
        );

        member = of(1L, "user@leafresh.com", "테스터");
        verification = of(null);
    }

    @Test
    @DisplayName("좋아요가 없는 경우 새로 생성한다")
    void likeVerification_createNewLike() {
        when(verificationRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(verification));
        when(memberRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(member));
        when(likeRepository.existsByVerificationIdAndMemberIdAndDeletedAtIsNull(1L, 1L)).thenReturn(false);
        when(likeRepository.findByVerificationIdAndMemberId(1L, 1L)).thenReturn(Optional.empty());

        boolean result = service.likeVerification(1L, 1L);

        assertThat(result).isTrue();
        verify(likeRepository).save(any(Like.class));
        verify(redisService).increaseVerificationLikeCount(1L);
    }

    @Test
    @DisplayName("soft delete된 좋아요가 있는 경우 복구한다")
    void likeVerification_restoreDeletedLike() {
        Like like = mock(Like.class);
        when(like.isDeleted()).thenReturn(true);

        when(verificationRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(verification));
        when(memberRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(member));
        when(likeRepository.existsByVerificationIdAndMemberIdAndDeletedAtIsNull(1L, 1L)).thenReturn(false);
        when(likeRepository.findByVerificationIdAndMemberId(1L, 1L)).thenReturn(Optional.of(like));

        boolean result = service.likeVerification(1L, 1L);

        assertThat(result).isTrue();
        verify(like).restoreLike();
        verify(redisService).increaseVerificationLikeCount(1L);
    }

    @Test
    @DisplayName("이미 좋아요가 되어 있는 경우 아무 작업 없이 true 반환")
    void likeVerification_alreadyExists() {
        when(verificationRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(verification));
        when(memberRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(member));
        when(likeRepository.existsByVerificationIdAndMemberIdAndDeletedAtIsNull(1L, 1L)).thenReturn(true);

        boolean result = service.likeVerification(1L, 1L);

        assertThat(result).isTrue();
        verify(likeRepository, never()).save(any());
        verify(redisService, never()).increaseVerificationLikeCount(anyLong());
    }

    @Test
    @DisplayName("좋아요 취소 시 삭제하고 false 반환")
    void cancelLike_success() {
        Like like = mock(Like.class);

        when(verificationRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(verification));
        when(memberRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(member));
        when(likeRepository.findByVerificationIdAndMemberIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(like));

        boolean result = service.cancelLike(1L, 1L);

        assertThat(result).isFalse();
        verify(like).softDelete();
        verify(redisService).decreaseVerificationLikeCount(1L);
    }

    @Test
    @DisplayName("좋아요 취소 시 이미 취소된 상태라면 false 반환")
    void cancelLike_alreadyDeleted() {
        when(verificationRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(verification));
        when(memberRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(member));
        when(likeRepository.findByVerificationIdAndMemberIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.empty());

        boolean result = service.cancelLike(1L, 1L);

        assertThat(result).isFalse();
        verify(redisService, never()).decreaseVerificationLikeCount(anyLong());
    }

    @Test
    @DisplayName("좋아요 시 인증을 찾지 못하면 예외 발생")
    void likeVerification_fail_verificationNotFound() {
        when(verificationRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        CustomException ex = catchThrowableOfType(() ->
                service.likeVerification(1L, 1L), CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(VerificationErrorCode.VERIFICATION_DETAIL_NOT_FOUND);
    }

    @Test
    @DisplayName("좋아요 시 멤버를 찾지 못하면 예외 발생")
    void likeVerification_fail_memberNotFound() {
        when(verificationRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(verification));
        when(memberRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        CustomException ex = catchThrowableOfType(() ->
                service.likeVerification(1L, 1L), CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(GlobalErrorCode.UNAUTHORIZED);
    }
}
