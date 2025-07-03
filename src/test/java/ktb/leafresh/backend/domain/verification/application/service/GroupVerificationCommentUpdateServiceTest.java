package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.verification.domain.entity.Comment;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.CommentRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.GroupVerificationCommentCreateRequestDto;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.CommentUpdateResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class GroupVerificationCommentUpdateServiceTest {

    @Mock
    private GroupChallengeVerificationRepository verificationRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private GroupVerificationCommentUpdateService updateService;

    @Test
    @DisplayName("댓글 수정에 성공한다")
    void updateComment_withValidInput_returnsUpdatedResponse() {
        // given
        Long challengeId = 1L;
        Long verificationId = 10L;
        Long commentId = 100L;
        Long memberId = 999L;
        String newContent = "수정된 댓글 내용";

        Member member = MemberFixture.of();
        ReflectionTestUtils.setField(member, "id", memberId);

        GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(null);

        Comment comment = Comment.builder()
                .verification(verification)
                .member(member)
                .content("기존 댓글")
                .build();
        ReflectionTestUtils.setField(comment, "id", commentId);
        ReflectionTestUtils.setField(comment, "createdAt", LocalDateTime.of(2024, 1, 1, 12, 0));
        ReflectionTestUtils.setField(comment, "updatedAt", LocalDateTime.of(2024, 1, 1, 12, 0));

        given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).willReturn(Optional.of(verification));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(commentRepository.findByParentCommentAndDeletedAtIsNull(comment)).willReturn(List.of());

        // when
        GroupVerificationCommentCreateRequestDto dto = new GroupVerificationCommentCreateRequestDto(newContent);
        CommentUpdateResponseDto result = updateService.updateComment(challengeId, verificationId, commentId, memberId, dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(commentId);
        assertThat(result.content()).isEqualTo(newContent);
        assertThat(result.nickname()).isEqualTo(member.getNickname());
        assertThat(result.profileImageUrl()).isEqualTo(member.getImageUrl());
        assertThat(result.deleted()).isFalse();
        assertThat(result.replies()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 인증이면 예외 발생")
    void updateComment_withInvalidVerification_throwsException() {
        // given
        Long verificationId = 10L;
        given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                updateService.updateComment(1L, verificationId, 100L, 999L, new GroupVerificationCommentCreateRequestDto("수정"))
        )
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(VerificationErrorCode.VERIFICATION_DETAIL_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 댓글이면 예외 발생")
    void updateComment_withInvalidComment_throwsException() {
        // given
        Long verificationId = 10L;
        GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(null);

        given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).willReturn(Optional.of(verification));
        given(commentRepository.findById(100L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                updateService.updateComment(1L, verificationId, 100L, 999L, new GroupVerificationCommentCreateRequestDto("수정"))
        )
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(VerificationErrorCode.COMMENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("본인의 댓글이 아니면 예외 발생")
    void updateComment_notMine_throwsAccessDenied() {
        // given
        Long memberId = 1L;
        Long anotherId = 2L;

        Member other = MemberFixture.of();
        ReflectionTestUtils.setField(other, "id", anotherId);

        GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(null);

        Comment comment = Comment.builder()
                .verification(verification)
                .member(other)
                .content("다른 사람 댓글")
                .build();
        ReflectionTestUtils.setField(comment, "id", 100L);

        given(verificationRepository.findByIdAndDeletedAtIsNull(10L)).willReturn(Optional.of(verification));
        given(commentRepository.findById(100L)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() ->
                updateService.updateComment(1L, 10L, 100L, memberId, new GroupVerificationCommentCreateRequestDto("수정"))
        )
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(GlobalErrorCode.ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("이미 삭제된 댓글은 수정할 수 없다")
    void updateComment_withDeletedComment_throwsException() {
        // given
        Long memberId = 1L;

        Member member = MemberFixture.of();
        ReflectionTestUtils.setField(member, "id", memberId);

        GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(null);

        Comment comment = Comment.builder()
                .verification(verification)
                .member(member)
                .content("삭제된 댓글")
                .build();
        ReflectionTestUtils.setField(comment, "id", 100L);
        ReflectionTestUtils.setField(comment, "deletedAt", LocalDateTime.of(2024, 1, 1, 0, 0));

        given(verificationRepository.findByIdAndDeletedAtIsNull(10L)).willReturn(Optional.of(verification));
        given(commentRepository.findById(100L)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() ->
                updateService.updateComment(1L, 10L, 100L, memberId, new GroupVerificationCommentCreateRequestDto("수정"))
        )
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(VerificationErrorCode.CANNOT_EDIT_DELETED_COMMENT.getMessage());
    }

    @Test
    @DisplayName("댓글 수정 중 예기치 못한 예외 발생 시 COMMENT_UPDATE_FAILED 예외 발생")
    void updateComment_whenUnexpectedExceptionThrown_throwsCreateFailed() {
        // given
        Long memberId = 1L;

        Member member = MemberFixture.of();
        ReflectionTestUtils.setField(member, "id", memberId);

        GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(null);

        Comment comment = Comment.builder()
                .verification(verification)
                .member(member)
                .content("기존 댓글")
                .build();
        ReflectionTestUtils.setField(comment, "id", 100L);

        given(verificationRepository.findByIdAndDeletedAtIsNull(10L)).willReturn(Optional.of(verification));
        given(commentRepository.findById(100L)).willReturn(Optional.of(comment));
        given(commentRepository.findByParentCommentAndDeletedAtIsNull(comment)).willThrow(new RuntimeException("DB ERROR"));

        // when & then
        assertThatThrownBy(() ->
                updateService.updateComment(1L, 10L, 100L, memberId, new GroupVerificationCommentCreateRequestDto("수정"))
        )
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(VerificationErrorCode.COMMENT_UPDATE_FAILED.getMessage());
    }
}
