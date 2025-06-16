package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.verification.domain.entity.Comment;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.CommentRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.GroupVerificationCommentCreateRequestDto;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.CommentResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import ktb.leafresh.backend.global.util.redis.VerificationStatRedisLuaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static ktb.leafresh.backend.support.fixture.GroupChallengeVerificationFixture.of;
import static ktb.leafresh.backend.support.fixture.MemberFixture.of;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GroupVerificationCommentCreateServiceTest {

    private CommentRepository commentRepository;
    private GroupChallengeVerificationRepository verificationRepository;
    private VerificationStatRedisLuaService redisLuaService;
    private ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository memberRepository;
    private GroupVerificationCommentCreateService service;

    private Member member;
    private GroupChallengeVerification verification;

    @BeforeEach
    void setUp() {
        commentRepository = mock(CommentRepository.class);
        verificationRepository = mock(GroupChallengeVerificationRepository.class);
        redisLuaService = mock(VerificationStatRedisLuaService.class);
        memberRepository = mock(ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository.class);

        service = new GroupVerificationCommentCreateService(
                commentRepository,
                verificationRepository,
                memberRepository,
                redisLuaService
        );

        member = of(1L, "test@leafresh.com", "테스터");
        verification = of(null);
    }

    @Test
    @DisplayName("일반 댓글을 생성한다")
    void createComment_success() {
        var requestDto = new GroupVerificationCommentCreateRequestDto("좋은 챌린지네요!");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(verificationRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(verification));

        // save()로 전달된 Comment 객체에 createdAt을 세팅해서 그대로 반환
        when(commentRepository.save(any(Comment.class))).thenAnswer((Answer<Comment>) invocation -> {
            Comment comment = invocation.getArgument(0);
            ReflectionTestUtils.setField(comment, "id", 1L); // 테스트용 ID도 세팅
            ReflectionTestUtils.setField(comment, "createdAt", LocalDateTime.now());
            return comment;
        });

        CommentResponseDto response = service.createComment(1L, 1L, 1L, requestDto);

        assertThat(response.content()).isEqualTo("좋은 챌린지네요!");
        verify(commentRepository).save(any(Comment.class));
        verify(redisLuaService).increaseVerificationCommentCount(1L);
    }

    @Test
    @DisplayName("대댓글을 생성한다")
    void createReply_success() {
        var requestDto = new GroupVerificationCommentCreateRequestDto("동의합니다!");
        Comment parent = Comment.builder()
                .id(999L)
                .content("좋은 챌린지네요!")
                .member(member)
                .verification(verification)
                .build();
        ReflectionTestUtils.setField(parent, "createdAt", LocalDateTime.now());

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(verificationRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(verification));
        when(commentRepository.findById(999L)).thenReturn(Optional.of(parent));

        // save 시점에 createdAt 세팅
        when(commentRepository.save(any(Comment.class))).thenAnswer((Answer<Comment>) invocation -> {
            Comment reply = invocation.getArgument(0);
            ReflectionTestUtils.setField(reply, "id", 1000L);
            ReflectionTestUtils.setField(reply, "createdAt", LocalDateTime.now());
            return reply;
        });

        CommentResponseDto response = service.createReply(1L, 1L, 999L, 1L, requestDto);

        assertThat(response.parentCommentId()).isEqualTo(999L);
        verify(commentRepository).save(any(Comment.class));
        verify(redisLuaService).increaseVerificationCommentCount(1L);
    }

    @Test
    @DisplayName("삭제된 댓글에 대댓글을 작성하려고 하면 예외가 발생한다")
    void createReply_fail_deletedParent() {
        // Given
        var requestDto = new GroupVerificationCommentCreateRequestDto("동의합니다!");
        Comment deletedParent = mock(Comment.class);
        when(deletedParent.getDeletedAt()).thenReturn(java.time.LocalDateTime.now());

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(verificationRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(verification));
        when(commentRepository.findById(999L)).thenReturn(Optional.of(deletedParent));

        // When
        CustomException exception = catchThrowableOfType(
                () -> service.createReply(1L, 1L, 999L, 1L, requestDto),
                CustomException.class
        );

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(VerificationErrorCode.CANNOT_REPLY_TO_DELETED_COMMENT);
    }

    @Test
    @DisplayName("댓글 생성 중 예상치 못한 예외가 발생하면 COMMENT_CREATE_FAILED 예외를 반환한다")
    void createComment_fail_by_unexpected_exception() {
        // Given
        var requestDto = new GroupVerificationCommentCreateRequestDto("좋은 챌린지네요!");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(verificationRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(verification));
        doThrow(new RuntimeException("Redis 에러")).when(redisLuaService).increaseVerificationCommentCount(anyLong());

        // When
        CustomException exception = catchThrowableOfType(
                () -> service.createComment(1L, 1L, 1L, requestDto),
                CustomException.class
        );

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(VerificationErrorCode.COMMENT_CREATE_FAILED);
    }

    @Test
    @DisplayName("대댓글 생성 중 예상치 못한 예외가 발생하면 COMMENT_CREATE_FAILED 예외를 반환한다")
    void createReply_fail_by_unexpected_exception() {
        // Given
        var requestDto = new GroupVerificationCommentCreateRequestDto("저도 동의합니다!");
        Comment parent = Comment.builder()
                .id(999L)
                .content("좋은 챌린지네요!")
                .member(member)
                .verification(verification)
                .build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(verificationRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(verification));
        when(commentRepository.findById(999L)).thenReturn(Optional.of(parent));
        doThrow(new RuntimeException("Redis 에러")).when(redisLuaService).increaseVerificationCommentCount(anyLong());

        // When
        CustomException exception = catchThrowableOfType(
                () -> service.createReply(1L, 1L, 999L, 1L, requestDto),
                CustomException.class
        );

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(VerificationErrorCode.COMMENT_CREATE_FAILED);
    }
}
