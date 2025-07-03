package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.verification.domain.entity.Comment;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.CommentRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.GroupVerificationCommentCreateRequestDto;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.CommentResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class GroupVerificationCommentCreateServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private GroupChallengeVerificationRepository verificationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private VerificationStatRedisLuaService verificationStatRedisLuaService;

    @InjectMocks
    private GroupVerificationCommentCreateService commentCreateService;

    @Test
    @DisplayName("댓글 생성에 성공한다")
    void createComment_withValidInput_returnsResponse() {
        // given
        Long memberId = 1L;
        Long verificationId = 10L;
        Long challengeId = 100L;
        String content = "좋은 인증이네요!";

        Member member = MemberFixture.of();
        GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(null);
        GroupVerificationCommentCreateRequestDto requestDto = new GroupVerificationCommentCreateRequestDto(content);

        given(commentRepository.save(any(Comment.class)))
                .willAnswer(invocation -> {
                    Comment saved = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", 999L);
                    ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.of(2024, 1, 1, 0, 0));
                    return saved;
                });

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).willReturn(Optional.of(verification));

        // when
        CommentResponseDto result = commentCreateService.createComment(challengeId, verificationId, memberId, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(999L); // 또는 ArgumentCaptor 활용
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.nickname()).isEqualTo(member.getNickname());
        assertThat(result.profileImageUrl()).isEqualTo(member.getImageUrl());
        assertThat(result.deleted()).isFalse();
        assertThat(result.parentCommentId()).isNull();

        then(memberRepository).should().findById(memberId);
        then(verificationRepository).should().findByIdAndDeletedAtIsNull(verificationId);
        then(commentRepository).should().save(any(Comment.class));
        then(verificationStatRedisLuaService).should().increaseVerificationCommentCount(verificationId);
    }

    @Test
    @DisplayName("댓글 생성 시 존재하지 않는 회원이면 예외가 발생한다")
    void createComment_withInvalidMember_throwsException() {
        // given
        Long memberId = 1L;
        Long verificationId = 10L;
        Long challengeId = 100L;
        GroupVerificationCommentCreateRequestDto requestDto = new GroupVerificationCommentCreateRequestDto("내용");

        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                commentCreateService.createComment(challengeId, verificationId, memberId, requestDto)
        )
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());

        then(memberRepository).should().findById(memberId);
        then(verificationRepository).shouldHaveNoInteractions();
        then(commentRepository).shouldHaveNoInteractions();
        then(verificationStatRedisLuaService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("댓글 생성 시 존재하지 않는 인증이면 예외가 발생한다")
    void createComment_withInvalidVerification_throwsException() {
        // given
        Long memberId = 1L;
        Long verificationId = 10L;
        Long challengeId = 100L;
        Member member = MemberFixture.of();
        GroupVerificationCommentCreateRequestDto requestDto = new GroupVerificationCommentCreateRequestDto("내용");

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                commentCreateService.createComment(challengeId, verificationId, memberId, requestDto)
        )
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(VerificationErrorCode.VERIFICATION_DETAIL_NOT_FOUND.getMessage());

        then(memberRepository).should().findById(memberId);
        then(verificationRepository).should().findByIdAndDeletedAtIsNull(verificationId);
        then(commentRepository).shouldHaveNoInteractions();
        then(verificationStatRedisLuaService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("대댓글 생성에 성공한다")
    void createReply_withValidInput_returnsResponse() {
        // given
        Long memberId = 1L;
        Long verificationId = 10L;
        Long parentCommentId = 999L;
        Long challengeId = 100L;
        String content = "좋은 의견 감사합니다!";

        Member member = MemberFixture.of();
        GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(null);

        Comment parentComment = Comment.builder()
                .verification(verification)
                .member(member)
                .content("부모 댓글")
                .build();
        ReflectionTestUtils.setField(parentComment, "id", parentCommentId);
        ReflectionTestUtils.setField(parentComment, "createdAt", LocalDateTime.of(2024, 1, 1, 0, 0));

        GroupVerificationCommentCreateRequestDto requestDto = new GroupVerificationCommentCreateRequestDto(content);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).willReturn(Optional.of(verification));
        given(commentRepository.findById(parentCommentId)).willReturn(Optional.of(parentComment));
        given(commentRepository.save(any(Comment.class)))
                .willAnswer(invocation -> {
                    Comment reply = invocation.getArgument(0);
                    ReflectionTestUtils.setField(reply, "id", 1000L);
                    ReflectionTestUtils.setField(reply, "createdAt", LocalDateTime.of(2024, 1, 2, 0, 0));
                    return reply;
                });

        // when
        CommentResponseDto result = commentCreateService.createReply(challengeId, verificationId, parentCommentId, memberId, requestDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1000L);
        assertThat(result.content()).isEqualTo(content);
        assertThat(result.parentCommentId()).isEqualTo(parentCommentId);
        assertThat(result.deleted()).isFalse();
        assertThat(result.nickname()).isEqualTo(member.getNickname());
        assertThat(result.profileImageUrl()).isEqualTo(member.getImageUrl());

        then(memberRepository).should().findById(memberId);
        then(verificationRepository).should().findByIdAndDeletedAtIsNull(verificationId);
        then(commentRepository).should().findById(parentCommentId);
        then(commentRepository).should().save(any(Comment.class));
        then(verificationStatRedisLuaService).should().increaseVerificationCommentCount(verificationId);
    }

    @Test
    @DisplayName("대댓글 생성 시 존재하지 않는 부모 댓글이면 예외가 발생한다")
    void createReply_withInvalidParentComment_throwsException() {
        // given
        Long memberId = 1L;
        Long verificationId = 10L;
        Long parentCommentId = 999L;
        Long challengeId = 100L;
        Member member = MemberFixture.of();
        GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(null);
        GroupVerificationCommentCreateRequestDto requestDto = new GroupVerificationCommentCreateRequestDto("대댓글");

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).willReturn(Optional.of(verification));
        given(commentRepository.findById(parentCommentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                commentCreateService.createReply(challengeId, verificationId, parentCommentId, memberId, requestDto)
        )
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(VerificationErrorCode.COMMENT_NOT_FOUND.getMessage());

        then(memberRepository).should().findById(memberId);
        then(verificationRepository).should().findByIdAndDeletedAtIsNull(verificationId);
        then(commentRepository).should().findById(parentCommentId);
        then(commentRepository).should(never()).save(any());
        then(verificationStatRedisLuaService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("삭제된 부모 댓글에는 대댓글을 달 수 없다")
    void createReply_withDeletedParentComment_throwsException() {
        // given
        Long memberId = 1L;
        Long verificationId = 10L;
        Long parentCommentId = 999L;
        Long challengeId = 100L;
        Member member = MemberFixture.of();
        GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(null);

        Comment deletedParent = Comment.builder()
                .verification(verification)
                .member(member)
                .content("삭제된 댓글")
                .build();
        ReflectionTestUtils.setField(deletedParent, "id", parentCommentId);
        ReflectionTestUtils.setField(deletedParent, "deletedAt", LocalDateTime.of(2024, 3, 1, 0, 0));

        GroupVerificationCommentCreateRequestDto requestDto = new GroupVerificationCommentCreateRequestDto("대댓글");

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).willReturn(Optional.of(verification));
        given(commentRepository.findById(parentCommentId)).willReturn(Optional.of(deletedParent));

        // when & then
        assertThatThrownBy(() ->
                commentCreateService.createReply(challengeId, verificationId, parentCommentId, memberId, requestDto)
        )
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(VerificationErrorCode.CANNOT_REPLY_TO_DELETED_COMMENT.getMessage());

        then(memberRepository).should().findById(memberId);
        then(verificationRepository).should().findByIdAndDeletedAtIsNull(verificationId);
        then(commentRepository).should().findById(parentCommentId);
        then(commentRepository).should(never()).save(any());
        then(verificationStatRedisLuaService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("댓글 생성 중 예기치 못한 예외가 발생하면 COMMENT_CREATE_FAILED 예외가 발생한다")
    void createComment_whenUnexpectedExceptionThrown_throwsCreateFailedException() {
        // given
        Long memberId = 1L;
        Long verificationId = 10L;
        Long challengeId = 100L;

        Member member = MemberFixture.of();
        GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(null);
        GroupVerificationCommentCreateRequestDto requestDto = new GroupVerificationCommentCreateRequestDto("예외 테스트");

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).willReturn(Optional.of(verification));
        given(commentRepository.save(any(Comment.class))).willThrow(new RuntimeException("DB ERROR"));

        // when & then
        assertThatThrownBy(() ->
                commentCreateService.createComment(challengeId, verificationId, memberId, requestDto)
        )
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(VerificationErrorCode.COMMENT_CREATE_FAILED.getMessage());

        then(memberRepository).should().findById(memberId);
        then(verificationRepository).should().findByIdAndDeletedAtIsNull(verificationId);
        then(commentRepository).should().save(any(Comment.class));
        then(verificationStatRedisLuaService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("대댓글 생성 중 예기치 못한 예외가 발생하면 COMMENT_CREATE_FAILED 예외가 발생한다")
    void createReply_whenUnexpectedExceptionThrown_throwsCreateFailedException() {
        // given
        Long memberId = 1L;
        Long verificationId = 10L;
        Long parentCommentId = 999L;
        Long challengeId = 100L;

        Member member = MemberFixture.of();
        GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(null);
        Comment parent = Comment.builder().verification(verification).member(member).content("부모 댓글").build();

        ReflectionTestUtils.setField(parent, "id", parentCommentId);

        GroupVerificationCommentCreateRequestDto requestDto = new GroupVerificationCommentCreateRequestDto("대댓글 예외");

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId)).willReturn(Optional.of(verification));
        given(commentRepository.findById(parentCommentId)).willReturn(Optional.of(parent));
        given(commentRepository.save(any(Comment.class))).willThrow(new RuntimeException("DB ERROR"));

        // when & then
        assertThatThrownBy(() ->
                commentCreateService.createReply(challengeId, verificationId, parentCommentId, memberId, requestDto)
        )
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(VerificationErrorCode.COMMENT_CREATE_FAILED.getMessage());

        then(memberRepository).should().findById(memberId);
        then(verificationRepository).should().findByIdAndDeletedAtIsNull(verificationId);
        then(commentRepository).should().findById(parentCommentId);
        then(commentRepository).should().save(any(Comment.class));
        then(verificationStatRedisLuaService).shouldHaveNoInteractions();
    }
}
