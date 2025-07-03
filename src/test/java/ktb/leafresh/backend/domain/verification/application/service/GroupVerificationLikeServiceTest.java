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
import ktb.leafresh.backend.support.fixture.GroupChallengeVerificationFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class GroupVerificationLikeServiceTest {

    @Mock
    private GroupChallengeVerificationRepository verificationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private VerificationStatRedisLuaService verificationStatRedisLuaService;

    @InjectMocks
    private GroupVerificationLikeService likeService;

    private final Long memberId = 1L;
    private final Long verificationId = 10L;
    private Member member;
    private GroupChallengeVerification verification;

    @BeforeEach
    void setUp() {
        member = MemberFixture.of();
        verification = GroupChallengeVerificationFixture.of(null);
    }

    @Test
    @DisplayName("이미 좋아요가 존재하면 true를 반환한다")
    void likeVerification_alreadyExists_returnsTrue() {
        // given
        given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).willReturn(Optional.of(verification));
        given(memberRepository.findByIdAndDeletedAtIsNull(memberId)).willReturn(Optional.of(member));
        given(likeRepository.existsByVerificationIdAndMemberIdAndDeletedAtIsNull(verificationId, memberId)).willReturn(true);

        // when
        boolean result = likeService.likeVerification(verificationId, memberId);

        // then
        assertThat(result).isTrue();
        then(likeRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("삭제된 좋아요가 있으면 복구하고 true를 반환한다")
    void likeVerification_softDeletedLike_restoreAndReturnTrue() {
        // given
        Like deletedLike = Like.builder().verification(verification).member(member).build();
        deletedLike.softDelete();

        given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).willReturn(Optional.of(verification));
        given(memberRepository.findByIdAndDeletedAtIsNull(memberId)).willReturn(Optional.of(member));
        given(likeRepository.existsByVerificationIdAndMemberIdAndDeletedAtIsNull(verificationId, memberId)).willReturn(false);
        given(likeRepository.findByVerificationIdAndMemberId(verificationId, memberId)).willReturn(Optional.of(deletedLike));

        // when
        boolean result = likeService.likeVerification(verificationId, memberId);

        // then
        assertThat(result).isTrue();
        assertThat(deletedLike.isDeleted()).isFalse();
        then(verificationStatRedisLuaService).should().increaseVerificationLikeCount(verificationId);
    }

    @Test
    @DisplayName("좋아요가 없으면 새로 생성하고 true를 반환한다")
    void likeVerification_createNewLike_returnsTrue() {
        // given
        given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).willReturn(Optional.of(verification));
        given(memberRepository.findByIdAndDeletedAtIsNull(memberId)).willReturn(Optional.of(member));
        given(likeRepository.existsByVerificationIdAndMemberIdAndDeletedAtIsNull(verificationId, memberId)).willReturn(false);

        given(likeRepository.findByVerificationIdAndMemberId(verificationId, memberId)).willReturn(Optional.empty());

        // when
        boolean result = likeService.likeVerification(verificationId, memberId);

        // then
        assertThat(result).isTrue();
        then(likeRepository).should().save(any(Like.class));
        then(verificationStatRedisLuaService).should().increaseVerificationLikeCount(verificationId);
    }

    @Test
    @DisplayName("좋아요 취소 - 존재하지 않으면 false 반환")
    void cancelLike_notExists_returnsFalse() {
        // given
        given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).willReturn(Optional.of(verification));
        given(memberRepository.findByIdAndDeletedAtIsNull(memberId)).willReturn(Optional.of(member));

        given(likeRepository.findByVerificationIdAndMemberIdAndDeletedAtIsNull(verificationId, memberId)).willReturn(Optional.empty());

        // when
        boolean result = likeService.cancelLike(verificationId, memberId);

        // then
        assertThat(result).isFalse();
        then(verificationStatRedisLuaService).should(never()).decreaseVerificationLikeCount(anyLong());
    }

    @Test
    @DisplayName("좋아요 취소 - 정상 취소되면 false 반환")
    void cancelLike_exists_softDeleteAndReturnFalse() {
        // given
        Like like = Like.builder().verification(verification).member(member).build();

        given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).willReturn(Optional.of(verification));
        given(memberRepository.findByIdAndDeletedAtIsNull(memberId)).willReturn(Optional.of(member));
        given(likeRepository.findByVerificationIdAndMemberIdAndDeletedAtIsNull(verificationId, memberId)).willReturn(Optional.ofNullable(like));

        // when
        boolean result = likeService.cancelLike(verificationId, memberId);

        // then
        assertThat(result).isFalse();
        assertThat(like.isDeleted()).isTrue();
        then(verificationStatRedisLuaService).should().decreaseVerificationLikeCount(verificationId);
    }

    @Test
    @DisplayName("좋아요 대상 인증이 존재하지 않으면 예외 발생")
    void likeVerification_invalidVerificationId_throwsException() {
        // given
        given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> likeService.likeVerification(verificationId, memberId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(VerificationErrorCode.VERIFICATION_DETAIL_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("좋아요 시 회원이 존재하지 않으면 예외 발생")
    void likeVerification_invalidMemberId_throwsException() {
        // given
        given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).willReturn(Optional.of(verification));
        given(memberRepository.findByIdAndDeletedAtIsNull(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> likeService.likeVerification(verificationId, memberId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(GlobalErrorCode.UNAUTHORIZED.getMessage());
    }
}
