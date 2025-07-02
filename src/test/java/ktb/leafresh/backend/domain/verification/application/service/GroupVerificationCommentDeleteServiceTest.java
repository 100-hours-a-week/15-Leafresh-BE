//package ktb.leafresh.backend.domain.verification.application.service;
//
//import ktb.leafresh.backend.domain.verification.domain.entity.Comment;
//import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
//import ktb.leafresh.backend.domain.verification.infrastructure.repository.CommentRepository;
//import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
//import ktb.leafresh.backend.global.exception.CustomException;
//import ktb.leafresh.backend.global.exception.GlobalErrorCode;
//import ktb.leafresh.backend.global.exception.VerificationErrorCode;
//import ktb.leafresh.backend.global.util.redis.VerificationStatRedisLuaService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import static ktb.leafresh.backend.support.fixture.GroupChallengeVerificationFixture.of;
//import static ktb.leafresh.backend.support.fixture.MemberFixture.of;
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class GroupVerificationCommentDeleteServiceTest {
//
//    private GroupChallengeVerificationRepository verificationRepository;
//    private CommentRepository commentRepository;
//    private VerificationStatRedisLuaService redisLuaService;
//    private GroupVerificationCommentDeleteService service;
//
//    private final Long memberId = 1L;
//    private final Long challengeId = 2L;
//    private final Long verificationId = 3L;
//    private final Long commentId = 4L;
//
//    @BeforeEach
//    void setUp() {
//        verificationRepository = mock(GroupChallengeVerificationRepository.class);
//        commentRepository = mock(CommentRepository.class);
//        redisLuaService = mock(VerificationStatRedisLuaService.class);
//
//        service = new GroupVerificationCommentDeleteService(
//                verificationRepository,
//                commentRepository,
//                redisLuaService
//        );
//    }
//
//    @Test
//    @DisplayName("댓글 삭제 성공")
//    void deleteComment_success() {
//        var member = of(memberId, "test@leafresh.com", "테스터");
//        var verification = of(null);
//        var comment = Comment.builder()
//                .id(commentId)
//                .member(member)
//                .verification(verification)
//                .content("삭제할 댓글")
//                .build();
//
//        when(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).thenReturn(Optional.of(verification));
//        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
//
//        service.deleteComment(challengeId, verificationId, commentId, memberId);
//
//        assertThat(comment.isDeleted()).isTrue();
//        verify(redisLuaService).decreaseVerificationCommentCount(verificationId);
//    }
//
//    @Test
//    @DisplayName("검증 정보가 없으면 예외 발생")
//    void deleteComment_fail_verification_not_found() {
//        when(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).thenReturn(Optional.empty());
//
//        CustomException ex = catchThrowableOfType(() ->
//                        service.deleteComment(challengeId, verificationId, commentId, memberId),
//                CustomException.class
//        );
//
//        assertThat(ex.getErrorCode()).isEqualTo(VerificationErrorCode.VERIFICATION_DETAIL_NOT_FOUND);
//    }
//
//    @Test
//    @DisplayName("댓글 정보가 없으면 예외 발생")
//    void deleteComment_fail_comment_not_found() {
//        when(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).thenReturn(Optional.of(of(null)));
//        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());
//
//        CustomException ex = catchThrowableOfType(() ->
//                        service.deleteComment(challengeId, verificationId, commentId, memberId),
//                CustomException.class
//        );
//
//        assertThat(ex.getErrorCode()).isEqualTo(VerificationErrorCode.COMMENT_NOT_FOUND);
//    }
//
//    @Test
//    @DisplayName("다른 사용자가 댓글 삭제 시도하면 예외 발생")
//    void deleteComment_fail_access_denied() {
//        var wrongMember = of(999L, "other@leafresh.com", "다른 사람");
//        var comment = Comment.builder()
//                .id(commentId)
//                .member(wrongMember)
//                .verification(of(null))
//                .content("댓글")
//                .build();
//
//        when(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).thenReturn(Optional.of(of(null)));
//        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
//
//        CustomException ex = catchThrowableOfType(() ->
//                        service.deleteComment(challengeId, verificationId, commentId, memberId),
//                CustomException.class
//        );
//
//        assertThat(ex.getErrorCode()).isEqualTo(GlobalErrorCode.ACCESS_DENIED);
//    }
//
//    @Test
//    @DisplayName("이미 삭제된 댓글은 삭제할 수 없음")
//    void deleteComment_fail_already_deleted() {
//        var member = of(memberId, "test@leafresh.com", "테스터");
//        var comment = Comment.builder()
//                .id(commentId)
//                .member(member)
//                .verification(of(null))
//                .content("삭제된 댓글")
//                .build();
//
//        // soft delete 처리
//        comment.softDelete();
//
//        when(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).thenReturn(Optional.of(of(null)));
//        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
//
//        CustomException ex = catchThrowableOfType(() ->
//                        service.deleteComment(challengeId, verificationId, commentId, memberId),
//                CustomException.class
//        );
//
//        assertThat(ex.getErrorCode()).isEqualTo(VerificationErrorCode.CANNOT_EDIT_DELETED_COMMENT);
//    }
//
//    @Test
//    @DisplayName("예상치 못한 예외가 발생하면 COMMENT_UPDATE_FAILED 예외 발생")
//    void deleteComment_fail_by_unexpected_exception() {
//        var member = of(memberId, "test@leafresh.com", "테스터");
//        var verification = of(null);
//        var comment = Comment.builder()
//                .id(commentId)
//                .member(member)
//                .verification(verification)
//                .content("댓글")
//                .build();
//
//        when(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).thenReturn(Optional.of(verification));
//        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
//        doThrow(new RuntimeException("Redis 에러")).when(redisLuaService).decreaseVerificationCommentCount(verificationId);
//
//        CustomException ex = catchThrowableOfType(() ->
//                        service.deleteComment(challengeId, verificationId, commentId, memberId),
//                CustomException.class
//        );
//
//        assertThat(ex.getErrorCode()).isEqualTo(VerificationErrorCode.COMMENT_UPDATE_FAILED);
//    }
//}
