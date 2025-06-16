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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static ktb.leafresh.backend.support.fixture.GroupChallengeVerificationFixture.of;
import static ktb.leafresh.backend.support.fixture.MemberFixture.of;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GroupVerificationCommentUpdateServiceTest {

    private CommentRepository commentRepository;
    private GroupChallengeVerificationRepository verificationRepository;
    private GroupVerificationCommentUpdateService service;

    private Member member;
    private GroupChallengeVerification verification;
    private Comment comment;

    @BeforeEach
    void setUp() {
        commentRepository = mock(CommentRepository.class);
        verificationRepository = mock(GroupChallengeVerificationRepository.class);

        service = new GroupVerificationCommentUpdateService(
                verificationRepository,
                commentRepository
        );

        member = of(1L, "user@leafresh.com", "테스터");
        verification = of(null);

        comment = Comment.builder()
                .id(100L)
                .content("기존 댓글 내용")
                .member(member)
                .verification(verification)
                .build();
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_success() {
        // given
        var dto = new GroupVerificationCommentCreateRequestDto("수정된 댓글 내용");

        when(verificationRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(verification));
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));
        when(commentRepository.findByParentCommentAndDeletedAtIsNull(comment)).thenReturn(List.of());

        // 수정 로직 후 updatedAt이 null인 상태이므로, 테스트에서 수동 세팅
        ReflectionTestUtils.setField(comment, "updatedAt", LocalDateTime.now());

        // when
        CommentUpdateResponseDto response = service.updateComment(1L, 1L, 100L, 1L, dto);

        // then
        assertThat(response.content()).isEqualTo("수정된 댓글 내용");
        verify(commentRepository, times(1)).findByParentCommentAndDeletedAtIsNull(comment);
    }

    @Test
    @DisplayName("댓글을 찾을 수 없으면 예외 발생")
    void updateComment_fail_comment_not_found() {
        when(verificationRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(verification));
        when(commentRepository.findById(100L)).thenReturn(Optional.empty());

        CustomException ex = catchThrowableOfType(() ->
                        service.updateComment(1L, 1L, 100L, 1L, new GroupVerificationCommentCreateRequestDto("업데이트")),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(VerificationErrorCode.COMMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("다른 사용자가 댓글 수정 시도 시 예외 발생")
    void updateComment_fail_access_denied() {
        Member other = of(2L, "other@leafresh.com", "다른사람");
        comment = Comment.builder()
                .id(100L)
                .content("내용")
                .member(other)
                .verification(verification)
                .build();

        when(verificationRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(verification));
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

        CustomException ex = catchThrowableOfType(() ->
                        service.updateComment(1L, 1L, 100L, 1L, new GroupVerificationCommentCreateRequestDto("업데이트")),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(GlobalErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("삭제된 댓글 수정 시도 시 예외 발생")
    void updateComment_fail_deleted_comment() {
        comment.softDelete();

        when(verificationRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(verification));
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comment));

        CustomException ex = catchThrowableOfType(() ->
                        service.updateComment(1L, 1L, 100L, 1L, new GroupVerificationCommentCreateRequestDto("업데이트")),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(VerificationErrorCode.CANNOT_EDIT_DELETED_COMMENT);
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 COMMENT_UPDATE_FAILED 반환")
    void updateComment_fail_by_unexpected_exception() {
        when(verificationRepository.findByIdAndDeletedAtIsNull(1L)).thenThrow(new RuntimeException("DB Error"));

        CustomException ex = catchThrowableOfType(() ->
                        service.updateComment(1L, 1L, 100L, 1L, new GroupVerificationCommentCreateRequestDto("업데이트")),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(VerificationErrorCode.COMMENT_UPDATE_FAILED);
    }
}
