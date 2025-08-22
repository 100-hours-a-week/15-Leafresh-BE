package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.verification.domain.entity.Comment;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.CommentRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import ktb.leafresh.backend.global.util.redis.VerificationStatRedisLuaService;
import ktb.leafresh.backend.support.fixture.GroupChallengeVerificationFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class GroupVerificationCommentDeleteServiceTest {

  @Mock private GroupChallengeVerificationRepository verificationRepository;

  @Mock private CommentRepository commentRepository;

  @Mock private VerificationStatRedisLuaService verificationStatRedisLuaService;

  @InjectMocks private GroupVerificationCommentDeleteService deleteService;

  @Test
  @DisplayName("댓글 삭제에 성공한다")
  void deleteComment_withValidInput_success() {
    // given
    Long challengeId = 1L;
    Long verificationId = 10L;
    Long commentId = 100L;
    Long memberId = 50L;

    Member member = MemberFixture.of();
    ReflectionTestUtils.setField(member, "id", memberId);
    GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(null);

    Comment comment =
        Comment.builder().member(member).verification(verification).content("삭제할 댓글").build();
    ReflectionTestUtils.setField(comment, "id", commentId);
    ReflectionTestUtils.setField(comment, "deletedAt", null); // 삭제되지 않은 상태

    given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId))
        .willReturn(Optional.of(verification));
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    // when
    deleteService.deleteComment(challengeId, verificationId, commentId, memberId);

    // then
    assertThat(comment.isDeleted()).isTrue();
    then(verificationRepository).should().findByIdAndDeletedAtIsNull(verificationId);
    then(commentRepository).should().findById(commentId);
    then(verificationStatRedisLuaService).should().decreaseVerificationCommentCount(verificationId);
  }

  @Test
  @DisplayName("존재하지 않는 인증 ID이면 예외 발생")
  void deleteComment_withInvalidVerification_throwsException() {
    // given
    Long challengeId = 1L;
    Long verificationId = 10L;
    Long commentId = 100L;
    Long memberId = 50L;

    given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId))
        .willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(
            () -> deleteService.deleteComment(challengeId, verificationId, commentId, memberId))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(VerificationErrorCode.VERIFICATION_DETAIL_NOT_FOUND.getMessage());

    then(verificationRepository).should().findByIdAndDeletedAtIsNull(verificationId);
    then(commentRepository).shouldHaveNoInteractions();
    then(verificationStatRedisLuaService).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("존재하지 않는 댓글 ID이면 예외 발생")
  void deleteComment_withInvalidComment_throwsException() {
    // given
    Long challengeId = 1L;
    Long verificationId = 10L;
    Long commentId = 100L;
    Long memberId = 50L;
    GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(null);

    given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId))
        .willReturn(Optional.of(verification));
    given(commentRepository.findById(commentId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(
            () -> deleteService.deleteComment(challengeId, verificationId, commentId, memberId))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(VerificationErrorCode.COMMENT_NOT_FOUND.getMessage());

    then(commentRepository).should().findById(commentId);
    then(verificationStatRedisLuaService).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("다른 사용자의 댓글은 삭제할 수 없다")
  void deleteComment_withOtherUser_throwsAccessDenied() {
    // given
    Long challengeId = 1L;
    Long verificationId = 10L;
    Long commentId = 100L;
    Long memberId = 1L;
    Long anotherMemberId = 2L;

    Member anotherMember = MemberFixture.of();
    ReflectionTestUtils.setField(anotherMember, "id", anotherMemberId);

    GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(null);

    Comment comment =
        Comment.builder().member(anotherMember).verification(verification).content("남의 댓글").build();
    ReflectionTestUtils.setField(comment, "id", commentId);

    given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId))
        .willReturn(Optional.of(verification));
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    // when & then
    assertThatThrownBy(
            () -> deleteService.deleteComment(challengeId, verificationId, commentId, memberId))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(GlobalErrorCode.ACCESS_DENIED.getMessage());

    then(verificationStatRedisLuaService).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("이미 삭제된 댓글이면 예외 발생")
  void deleteComment_whenAlreadyDeleted_throwsException() {
    // given
    Long challengeId = 1L;
    Long verificationId = 10L;
    Long commentId = 100L;
    Long memberId = 1L;

    Member member = MemberFixture.of();
    ReflectionTestUtils.setField(member, "id", memberId);

    GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(null);

    Comment comment =
        Comment.builder().member(member).verification(verification).content("이미 삭제된 댓글").build();
    ReflectionTestUtils.setField(comment, "id", commentId);
    ReflectionTestUtils.setField(comment, "deletedAt", LocalDateTime.of(2024, 1, 1, 0, 0));

    given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId))
        .willReturn(Optional.of(verification));
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    // when & then
    assertThatThrownBy(
            () -> deleteService.deleteComment(challengeId, verificationId, commentId, memberId))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(VerificationErrorCode.CANNOT_EDIT_DELETED_COMMENT.getMessage());

    then(verificationStatRedisLuaService).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("댓글 삭제 중 예기치 못한 예외가 발생하면 COMMENT_UPDATE_FAILED 예외 발생")
  void deleteComment_whenUnexpectedException_throwsCreateFailed() {
    // given
    Long challengeId = 1L;
    Long verificationId = 10L;
    Long commentId = 100L;
    Long memberId = 1L;

    Member member = MemberFixture.of();
    ReflectionTestUtils.setField(member, "id", memberId);
    GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(null);

    Comment comment =
        Comment.builder().member(member).verification(verification).content("예외 발생 댓글").build();
    ReflectionTestUtils.setField(comment, "id", commentId);
    ReflectionTestUtils.setField(comment, "deletedAt", null);

    given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId))
        .willReturn(Optional.of(verification));
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
    willThrow(new RuntimeException("Redis 오류"))
        .given(verificationStatRedisLuaService)
        .decreaseVerificationCommentCount(verificationId);

    // when & then
    assertThatThrownBy(
            () -> deleteService.deleteComment(challengeId, verificationId, commentId, memberId))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(VerificationErrorCode.COMMENT_UPDATE_FAILED.getMessage());

    then(verificationStatRedisLuaService).should().decreaseVerificationCommentCount(verificationId);
  }
}
