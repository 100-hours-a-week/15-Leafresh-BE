package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.verification.domain.entity.Comment;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;

public class CommentFixture {

  private static final String DEFAULT_CONTENT = "좋은 인증이네요!";

  /** 기본 댓글을 생성합니다. */
  public static Comment of() {
    Member member = MemberFixture.of();
    GroupChallengeVerification verification =
        GroupChallengeVerificationFixture.of(
            GroupChallengeParticipantRecordFixture.of(
                GroupChallengeFixture.of(member, GroupChallengeCategoryFixture.defaultCategory()),
                member));
    return of(verification, member, DEFAULT_CONTENT);
  }

  /** 지정된 인증과 회원으로 댓글을 생성합니다. */
  public static Comment of(GroupChallengeVerification verification, Member member) {
    return of(verification, member, DEFAULT_CONTENT);
  }

  /** 모든 매개변수를 지정하여 댓글을 생성합니다. */
  public static Comment of(GroupChallengeVerification verification, Member member, String content) {
    return Comment.builder()
        .verification(verification)
        .member(member)
        .content(content)
        .parentComment(null)
        .build();
  }

  /** 대댓글을 생성합니다. */
  public static Comment replyOf(
      GroupChallengeVerification verification,
      Member member,
      Comment parentComment,
      String content) {
    return Comment.builder()
        .verification(verification)
        .member(member)
        .content(content)
        .parentComment(parentComment)
        .build();
  }
}
