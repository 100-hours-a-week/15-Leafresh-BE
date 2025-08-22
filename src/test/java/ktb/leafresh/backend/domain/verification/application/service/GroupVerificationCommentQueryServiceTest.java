package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.verification.domain.entity.Comment;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.CommentRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.CommentSummaryResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
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
class GroupVerificationCommentQueryServiceTest {

  @Mock private GroupChallengeVerificationRepository verificationRepository;

  @Mock private CommentRepository commentRepository;

  @InjectMocks private GroupVerificationCommentQueryService commentQueryService;

  @Test
  @DisplayName("댓글 계층 목록 조회에 성공한다")
  void getComments_withValidInput_returnsHierarchy() {
    // given
    Long challengeId = 1L;
    Long verificationId = 10L;
    Long memberId = 99L;

    GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(null);
    Member member = MemberFixture.of();
    ReflectionTestUtils.setField(member, "id", memberId);

    // 부모 댓글
    Comment parent =
        Comment.builder().verification(verification).member(member).content("부모 댓글").build();
    ReflectionTestUtils.setField(parent, "id", 100L);
    ReflectionTestUtils.setField(parent, "createdAt", LocalDateTime.of(2024, 1, 1, 12, 0));
    ReflectionTestUtils.setField(parent, "updatedAt", LocalDateTime.of(2024, 1, 1, 12, 0));

    // 자식 댓글
    Comment child =
        Comment.builder()
            .verification(verification)
            .member(member)
            .parentComment(parent)
            .content("대댓글")
            .build();
    ReflectionTestUtils.setField(child, "id", 101L);
    ReflectionTestUtils.setField(child, "createdAt", LocalDateTime.of(2024, 1, 1, 12, 1));
    ReflectionTestUtils.setField(child, "updatedAt", LocalDateTime.of(2024, 1, 1, 12, 1));

    given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId))
        .willReturn(Optional.of(verification));
    given(commentRepository.findAllByVerificationIdWithMember(verificationId))
        .willReturn(List.of(parent, child));

    // when
    List<CommentSummaryResponseDto> result =
        commentQueryService.getComments(challengeId, verificationId, memberId);

    // then
    assertThat(result).hasSize(1);
    CommentSummaryResponseDto parentDto = result.get(0);
    assertThat(parentDto.getId()).isEqualTo(parent.getId());
    assertThat(parentDto.getReplies()).hasSize(1);
    assertThat(parentDto.getReplies().get(0).getId()).isEqualTo(child.getId());
    assertThat(parentDto.isMine()).isTrue();
    assertThat(parentDto.getReplies().get(0).isMine()).isTrue();

    then(verificationRepository).should().findByIdAndDeletedAtIsNull(verificationId);
    then(commentRepository).should().findAllByVerificationIdWithMember(verificationId);
  }

  @Test
  @DisplayName("존재하지 않는 인증 ID이면 예외 발생")
  void getComments_withInvalidVerification_throwsException() {
    // given
    Long challengeId = 1L;
    Long verificationId = 99L;
    Long loginMemberId = 1L;

    given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId))
        .willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(
            () -> commentQueryService.getComments(challengeId, verificationId, loginMemberId))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(VerificationErrorCode.VERIFICATION_DETAIL_NOT_FOUND.getMessage());

    then(verificationRepository).should().findByIdAndDeletedAtIsNull(verificationId);
    then(commentRepository).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("댓글이 하나도 없는 경우 빈 리스트를 반환한다")
  void getComments_withNoComments_returnsEmptyList() {
    // given
    Long challengeId = 1L;
    Long verificationId = 10L;
    Long loginMemberId = 1L;
    GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(null);

    given(verificationRepository.findByIdAndDeletedAtIsNull(verificationId))
        .willReturn(Optional.of(verification));
    given(commentRepository.findAllByVerificationIdWithMember(verificationId))
        .willReturn(List.of());

    // when
    List<CommentSummaryResponseDto> result =
        commentQueryService.getComments(challengeId, verificationId, loginMemberId);

    // then
    assertThat(result).isEmpty();

    then(verificationRepository).should().findByIdAndDeletedAtIsNull(verificationId);
    then(commentRepository).should().findAllByVerificationIdWithMember(verificationId);
  }
}
