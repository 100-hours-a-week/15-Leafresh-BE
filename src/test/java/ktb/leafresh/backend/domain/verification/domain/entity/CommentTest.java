package ktb.leafresh.backend.domain.verification.domain.entity;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.support.fixture.CommentFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeParticipantRecordFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeVerificationFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CommentTest {

  @Nested
  @DisplayName("Comment 생성")
  class CreateComment {

    @Test
    @DisplayName("일반 댓글을 정상적으로 생성한다")
    void createComment_withValidData_success() {
      // given
      Member member = MemberFixture.of();
      GroupChallengeVerification verification =
          GroupChallengeVerificationFixture.of(
              GroupChallengeParticipantRecordFixture.of(
                  GroupChallengeFixture.of(member, GroupChallengeCategoryFixture.defaultCategory()),
                  member));
      Member commenter = MemberFixture.of("commenter@test.com", "댓글작성자");
      String content = "정말 멋진 인증이네요!";

      // when
      Comment comment = CommentFixture.of(verification, commenter, content);

      // then
      assertThat(comment).isNotNull();
      assertThat(comment.getVerification()).isEqualTo(verification);
      assertThat(comment.getMember()).isEqualTo(commenter);
      assertThat(comment.getContent()).isEqualTo(content);
      assertThat(comment.getParentComment()).isNull();
    }

    @Test
    @DisplayName("대댓글을 정상적으로 생성한다")
    void createReplyComment_withValidData_success() {
      // given
      Member member = MemberFixture.of();
      GroupChallengeVerification verification =
          GroupChallengeVerificationFixture.of(
              GroupChallengeParticipantRecordFixture.of(
                  GroupChallengeFixture.of(member, GroupChallengeCategoryFixture.defaultCategory()),
                  member));
      Member originalCommenter = MemberFixture.of("original@test.com", "원댓글작성자");
      Member replier = MemberFixture.of("replier@test.com", "대댓글작성자");

      Comment parentComment = CommentFixture.of(verification, originalCommenter, "원댓글 내용");
      String replyContent = "동감합니다!";

      // when
      Comment replyComment =
          CommentFixture.replyOf(verification, replier, parentComment, replyContent);

      // then
      assertThat(replyComment).isNotNull();
      assertThat(replyComment.getVerification()).isEqualTo(verification);
      assertThat(replyComment.getMember()).isEqualTo(replier);
      assertThat(replyComment.getContent()).isEqualTo(replyContent);
      assertThat(replyComment.getParentComment()).isEqualTo(parentComment);
    }
  }

  @Nested
  @DisplayName("댓글 내용 수정")
  class UpdateContent {

    @Test
    @DisplayName("댓글 내용을 정상적으로 수정한다")
    void updateContent_withValidContent_success() {
      // given
      Comment comment = CommentFixture.of();
      String originalContent = comment.getContent();
      String newContent = "수정된 댓글 내용입니다.";

      // when
      comment.updateContent(newContent);

      // then
      assertThat(comment.getContent()).isEqualTo(newContent);
      assertThat(comment.getContent()).isNotEqualTo(originalContent);
    }

    @Test
    @DisplayName("빈 문자열로 댓글 내용을 수정한다")
    void updateContent_withEmptyString_success() {
      // given
      Comment comment = CommentFixture.of();
      String emptyContent = "";

      // when
      comment.updateContent(emptyContent);

      // then
      assertThat(comment.getContent()).isEqualTo(emptyContent);
    }

    @Test
    @DisplayName("null 값으로 댓글 내용을 수정한다")
    void updateContent_withNull_success() {
      // given
      Comment comment = CommentFixture.of();

      // when
      comment.updateContent(null);

      // then
      assertThat(comment.getContent()).isNull();
    }

    @Test
    @DisplayName("긴 텍스트로 댓글 내용을 수정한다")
    void updateContent_withLongText_success() {
      // given
      Comment comment = CommentFixture.of();
      String longContent = "매우 긴 댓글 내용입니다. ".repeat(50) + "이런 식으로 긴 내용도 정상적으로 처리되어야 합니다.";

      // when
      comment.updateContent(longContent);

      // then
      assertThat(comment.getContent()).isEqualTo(longContent);
    }
  }

  @Nested
  @DisplayName("댓글 계층 구조")
  class CommentHierarchy {

    @Test
    @DisplayName("부모 댓글과 자식 댓글의 관계를 확인한다")
    void verifyParentChildRelationship() {
      // given
      Member member = MemberFixture.of();
      GroupChallengeVerification verification =
          GroupChallengeVerificationFixture.of(
              GroupChallengeParticipantRecordFixture.of(
                  GroupChallengeFixture.of(member, GroupChallengeCategoryFixture.defaultCategory()),
                  member));
      Member parentCommenter = MemberFixture.of("parent@test.com", "부모댓글작성자");
      Member childCommenter = MemberFixture.of("child@test.com", "자식댓글작성자");

      Comment parentComment = CommentFixture.of(verification, parentCommenter, "이것은 부모 댓글입니다.");

      // when
      Comment childComment =
          CommentFixture.replyOf(verification, childCommenter, parentComment, "이것은 자식 댓글입니다.");

      // then
      assertThat(parentComment.getParentComment()).isNull();
      assertThat(childComment.getParentComment()).isEqualTo(parentComment);
      assertThat(childComment.getVerification()).isEqualTo(parentComment.getVerification());
    }
  }

  @Nested
  @DisplayName("댓글 Builder 패턴")
  class CommentBuilder {

    @Test
    @DisplayName("Builder 패턴으로 모든 필드를 설정하여 댓글을 생성한다")
    void buildComment_withAllFields_success() {
      // given
      Member member = MemberFixture.of();
      GroupChallengeVerification verification =
          GroupChallengeVerificationFixture.of(
              GroupChallengeParticipantRecordFixture.of(
                  GroupChallengeFixture.of(member, GroupChallengeCategoryFixture.defaultCategory()),
                  member));
      Member commenter = MemberFixture.of("builder@test.com", "빌더테스터");
      Comment parentComment = CommentFixture.of(verification, commenter);
      String content = "Builder로 생성된 댓글입니다.";

      // when
      Comment comment =
          Comment.builder()
              .verification(verification)
              .member(commenter)
              .parentComment(parentComment)
              .content(content)
              .build();

      // then
      assertThat(comment.getVerification()).isEqualTo(verification);
      assertThat(comment.getMember()).isEqualTo(commenter);
      assertThat(comment.getParentComment()).isEqualTo(parentComment);
      assertThat(comment.getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("Builder 패턴으로 필수 필드만 설정하여 댓글을 생성한다")
    void buildComment_withRequiredFields_success() {
      // given
      Member member = MemberFixture.of();
      GroupChallengeVerification verification =
          GroupChallengeVerificationFixture.of(
              GroupChallengeParticipantRecordFixture.of(
                  GroupChallengeFixture.of(member, GroupChallengeCategoryFixture.defaultCategory()),
                  member));
      Member commenter = MemberFixture.of("required@test.com", "필수필드테스터");
      String content = "필수 필드만 설정된 댓글입니다.";

      // when
      Comment comment =
          Comment.builder().verification(verification).member(commenter).content(content).build();

      // then
      assertThat(comment.getVerification()).isEqualTo(verification);
      assertThat(comment.getMember()).isEqualTo(commenter);
      assertThat(comment.getContent()).isEqualTo(content);
      assertThat(comment.getParentComment()).isNull();
    }
  }

  @Nested
  @DisplayName("댓글 불변성")
  class CommentImmutability {

    @Test
    @DisplayName("생성된 댓글의 verification과 member는 변경되지 않는다")
    void verifyImmutabilityOfCoreFields() {
      // given
      Member member = MemberFixture.of();
      GroupChallengeVerification verification =
          GroupChallengeVerificationFixture.of(
              GroupChallengeParticipantRecordFixture.of(
                  GroupChallengeFixture.of(member, GroupChallengeCategoryFixture.defaultCategory()),
                  member));
      Member originalMember = MemberFixture.of("original@test.com", "원본회원");
      Comment comment = CommentFixture.of(verification, originalMember);

      // when & then
      assertThat(comment.getVerification()).isEqualTo(verification);
      assertThat(comment.getMember()).isEqualTo(originalMember);
      // verification과 member 필드는 setter가 없어 변경 불가능함을 확인
    }

    @Test
    @DisplayName("content만 수정 가능하고 나머지 필드는 불변이다")
    void verifyOnlyContentIsModifiable() {
      // given
      Comment comment = CommentFixture.of();
      GroupChallengeVerification originalVerification = comment.getVerification();
      Member originalMember = comment.getMember();
      Comment originalParentComment = comment.getParentComment();

      // when
      comment.updateContent("새로운 내용");

      // then
      // content는 변경됨
      assertThat(comment.getContent()).isEqualTo("새로운 내용");
      // 다른 필드들은 변경되지 않음
      assertThat(comment.getVerification()).isEqualTo(originalVerification);
      assertThat(comment.getMember()).isEqualTo(originalMember);
      assertThat(comment.getParentComment()).isEqualTo(originalParentComment);
    }
  }

  @Nested
  @DisplayName("댓글 Edge Cases")
  class CommentEdgeCases {

    @Test
    @DisplayName("특수문자가 포함된 댓글 내용을 처리한다")
    void updateContent_withSpecialCharacters_success() {
      // given
      Comment comment = CommentFixture.of();
      String specialContent = "이모지 😊🎉 특수문자 !@#$%^&*() 한글 English 123";

      // when
      comment.updateContent(specialContent);

      // then
      assertThat(comment.getContent()).isEqualTo(specialContent);
    }

    @Test
    @DisplayName("줄바꿈이 포함된 댓글 내용을 처리한다")
    void updateContent_withLineBreaks_success() {
      // given
      Comment comment = CommentFixture.of();
      String multiLineContent = "첫 번째 줄\n두 번째 줄\n세 번째 줄";

      // when
      comment.updateContent(multiLineContent);

      // then
      assertThat(comment.getContent()).isEqualTo(multiLineContent);
      assertThat(comment.getContent()).contains("\n");
    }
  }
}
