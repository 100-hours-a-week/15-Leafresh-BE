//package ktb.leafresh.backend.domain.verification.application.service;
//
//import ktb.leafresh.backend.domain.verification.domain.entity.Comment;
//import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
//import ktb.leafresh.backend.domain.verification.infrastructure.repository.CommentRepository;
//import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
//import ktb.leafresh.backend.domain.verification.presentation.dto.response.CommentSummaryResponseDto;
//import ktb.leafresh.backend.global.exception.CustomException;
//import ktb.leafresh.backend.global.exception.VerificationErrorCode;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.util.List;
//import java.util.Optional;
//
//import static ktb.leafresh.backend.support.fixture.GroupChallengeVerificationFixture.of;
//import static ktb.leafresh.backend.support.fixture.MemberFixture.of;
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class GroupVerificationCommentQueryServiceTest {
//
//    private GroupChallengeVerificationRepository verificationRepository;
//    private CommentRepository commentRepository;
//    private GroupVerificationCommentQueryService service;
//
//    @BeforeEach
//    void setUp() {
//        verificationRepository = mock(GroupChallengeVerificationRepository.class);
//        commentRepository = mock(CommentRepository.class);
//        service = new GroupVerificationCommentQueryService(verificationRepository, commentRepository);
//    }
//
//    @Test
//    @DisplayName("댓글 목록을 정상 조회한다")
//    void getComments_success() {
//        // Given
//        Long challengeId = 1L;
//        Long verificationId = 2L;
//        Long loginMemberId = 3L;
//
//        GroupChallengeVerification verification = of(null);
//        when(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).thenReturn(Optional.of(verification));
//
//        var member = of(loginMemberId, "test@leafresh.com", "테스터");
//
//        var comment1 = Comment.builder()
//                .id(1L)
//                .member(member)
//                .verification(verification)
//                .content("댓글1")
//                .build();
//
//        var comment2 = Comment.builder()
//                .id(2L)
//                .member(member)
//                .verification(verification)
//                .content("댓글2")
//                .build();
//
//        var now = java.time.LocalDateTime.now();
//        ReflectionTestUtils.setField(comment1, "createdAt", now.minusSeconds(10));
//        ReflectionTestUtils.setField(comment1, "updatedAt", now.minusSeconds(5));
//
//        ReflectionTestUtils.setField(comment2, "createdAt", now);
//        ReflectionTestUtils.setField(comment2, "updatedAt", now);
//
//        when(commentRepository.findAllByVerificationIdWithMember(verificationId))
//                .thenReturn(List.of(comment1, comment2));
//
//        // When
//        List<CommentSummaryResponseDto> result = service.getComments(challengeId, verificationId, loginMemberId);
//
//        // Then
//        assertThat(result).hasSize(2);
//        assertThat(result.get(0).getContent()).isEqualTo("댓글1");
//        assertThat(result.get(1).getContent()).isEqualTo("댓글2");
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 인증 ID면 예외를 반환한다")
//    void getComments_fail_verificationNotFound() {
//        // Given
//        Long challengeId = 1L;
//        Long verificationId = 999L;
//        Long loginMemberId = 1L;
//
//        when(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).thenReturn(Optional.empty());
//
//        // When
//        CustomException exception = catchThrowableOfType(
//                () -> service.getComments(challengeId, verificationId, loginMemberId),
//                CustomException.class
//        );
//
//        // Then
//        assertThat(exception).isNotNull();
//        assertThat(exception.getErrorCode()).isEqualTo(VerificationErrorCode.VERIFICATION_DETAIL_NOT_FOUND);
//    }
//}
