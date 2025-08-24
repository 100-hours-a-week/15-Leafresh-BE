package ktb.leafresh.backend.domain.verification.domain.entity;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeParticipantRecordFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GroupChallengeVerification 엔티티 테스트")
class GroupChallengeVerificationTest {

  private GroupChallengeParticipantRecord participantRecord;

  @BeforeEach
  void setUp() {
    // 공통으로 사용할 객체들 미리 생성
    Member member = MemberFixture.of();
    GroupChallengeCategory category = GroupChallengeCategoryFixture.defaultCategory();
    GroupChallenge challenge = GroupChallengeFixture.of(member, category);
    participantRecord = GroupChallengeParticipantRecordFixture.of(challenge, member);
  }

  @Nested
  @DisplayName("생성 테스트")
  class CreationTest {

    @Test
    @DisplayName("빌더를 통해 정상적으로 생성된다")
    void shouldCreateSuccessfully() {
      // given
      String imageUrl = "https://example.com/image.jpg";
      String content = "인증 내용";
      ChallengeStatus status = ChallengeStatus.PENDING_APPROVAL;
      boolean rewarded = false;

      // when
      GroupChallengeVerification verification =
          GroupChallengeVerification.builder()
              .participantRecord(participantRecord)
              .imageUrl(imageUrl)
              .content(content)
              .status(status)
              .rewarded(rewarded)
              .build();

      // then
      assertThat(verification.getParticipantRecord()).isEqualTo(participantRecord);
      assertThat(verification.getImageUrl()).isEqualTo(imageUrl);
      assertThat(verification.getContent()).isEqualTo(content);
      assertThat(verification.getStatus()).isEqualTo(status);
      assertThat(verification.isRewarded()).isEqualTo(rewarded);
      assertThat(verification.getLikes()).isNotNull().isEmpty();
      assertThat(verification.getComments()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("빌더 생성 시 컬렉션 필드가 초기화된다")
    void shouldInitializeCollections() {
      // when
      GroupChallengeVerification verification =
          GroupChallengeVerification.builder()
              .participantRecord(participantRecord)
              .imageUrl("https://example.com/image.jpg")
              .content("인증 내용")
              .status(ChallengeStatus.PENDING_APPROVAL)
              .rewarded(false)
              .build();

      // then
      assertThat(verification.getLikes()).isNotNull().isInstanceOf(ArrayList.class);
      assertThat(verification.getComments()).isNotNull().isInstanceOf(ArrayList.class);
    }
  }

  @Nested
  @DisplayName("prePersist 테스트")
  class PrePersistTest {

    @Test
    @DisplayName("저장 전 기본값들이 정상적으로 설정된다")
    void shouldSetDefaultValuesOnPrePersist() {
      // given
      GroupChallengeVerification verification =
          GroupChallengeVerification.builder()
              .participantRecord(participantRecord)
              .imageUrl("https://example.com/image.jpg")
              .content("인증 내용")
              .status(ChallengeStatus.PENDING_APPROVAL)
              .rewarded(true) // 의도적으로 true로 설정
              .build();

      // viewCount, likeCount, commentCount를 의도적으로 다른 값으로 설정
      ReflectionTestUtils.setField(verification, "viewCount", 10);
      ReflectionTestUtils.setField(verification, "likeCount", 5);
      ReflectionTestUtils.setField(verification, "commentCount", 3);

      // when
      verification.prePersist();

      // then
      assertThat(verification.isRewarded()).isFalse();
      assertThat(verification.getViewCount()).isZero();
      assertThat(verification.getLikeCount()).isZero();
      assertThat(verification.getCommentCount()).isZero();
    }
  }

  @Nested
  @DisplayName("인증 완료 처리 테스트")
  class MarkVerifiedTest {

    @Test
    @DisplayName("성공 상태로 인증 완료 처리가 된다")
    void shouldMarkVerifiedWithSuccessStatus() {
      // given
      GroupChallengeVerification verification =
          GroupChallengeVerification.builder()
              .participantRecord(participantRecord)
              .imageUrl("https://example.com/image.jpg")
              .content("인증 내용")
              .status(ChallengeStatus.PENDING_APPROVAL)
              .rewarded(false)
              .build();

      LocalDateTime beforeTime = LocalDateTime.now().minusSeconds(1);

      // when
      verification.markVerified(ChallengeStatus.SUCCESS);

      // then
      LocalDateTime afterTime = LocalDateTime.now().plusSeconds(1);
      assertThat(verification.getStatus()).isEqualTo(ChallengeStatus.SUCCESS);
      assertThat(verification.getVerifiedAt()).isBetween(beforeTime, afterTime);
    }

    @Test
    @DisplayName("실패 상태로 인증 완료 처리가 된다")
    void shouldMarkVerifiedWithFailureStatus() {
      // given
      GroupChallengeVerification verification =
          GroupChallengeVerification.builder()
              .participantRecord(participantRecord)
              .imageUrl("https://example.com/image.jpg")
              .content("인증 내용")
              .status(ChallengeStatus.PENDING_APPROVAL)
              .rewarded(false)
              .build();

      LocalDateTime beforeTime = LocalDateTime.now().minusSeconds(1);

      // when
      verification.markVerified(ChallengeStatus.FAILURE);

      // then
      LocalDateTime afterTime = LocalDateTime.now().plusSeconds(1);
      assertThat(verification.getStatus()).isEqualTo(ChallengeStatus.FAILURE);
      assertThat(verification.getVerifiedAt()).isBetween(beforeTime, afterTime);
    }
  }

  @Nested
  @DisplayName("리워드 처리 테스트")
  class RewardTest {

    @Test
    @DisplayName("리워드 상태 확인이 정상적으로 작동한다")
    void shouldCheckRewardStatus() {
      // given
      GroupChallengeVerification verification =
          GroupChallengeVerification.builder()
              .participantRecord(participantRecord)
              .imageUrl("https://example.com/image.jpg")
              .content("인증 내용")
              .status(ChallengeStatus.SUCCESS)
              .rewarded(false)
              .build();

      // when & then
      assertThat(verification.isRewarded()).isFalse();
    }

    @Test
    @DisplayName("리워드 처리가 정상적으로 작동한다")
    void shouldMarkRewarded() {
      // given
      GroupChallengeVerification verification =
          GroupChallengeVerification.builder()
              .participantRecord(participantRecord)
              .imageUrl("https://example.com/image.jpg")
              .content("인증 내용")
              .status(ChallengeStatus.SUCCESS)
              .rewarded(false)
              .build();

      // when
      verification.markRewarded();

      // then
      assertThat(verification.isRewarded()).isTrue();
    }

    @Test
    @DisplayName("이미 리워드 처리된 경우도 정상적으로 작동한다")
    void shouldHandleAlreadyRewarded() {
      // given
      GroupChallengeVerification verification =
          GroupChallengeVerification.builder()
              .participantRecord(participantRecord)
              .imageUrl("https://example.com/image.jpg")
              .content("인증 내용")
              .status(ChallengeStatus.SUCCESS)
              .rewarded(true)
              .build();

      // when
      verification.markRewarded();

      // then
      assertThat(verification.isRewarded()).isTrue();
    }
  }

  @Nested
  @DisplayName("통계 필드 테스트")
  class StatisticsTest {

    @Test
    @DisplayName("조회수, 좋아요수, 댓글수 기본값이 0이다")
    void shouldHaveDefaultStatisticsValues() {
      // when
      GroupChallengeVerification verification =
          GroupChallengeVerification.builder()
              .participantRecord(participantRecord)
              .imageUrl("https://example.com/image.jpg")
              .content("인증 내용")
              .status(ChallengeStatus.PENDING_APPROVAL)
              .rewarded(false)
              .build();

      verification.prePersist(); // @PrePersist 메서드 호출

      // then
      assertThat(verification.getViewCount()).isZero();
      assertThat(verification.getLikeCount()).isZero();
      assertThat(verification.getCommentCount()).isZero();
    }
  }

  @Nested
  @DisplayName("필수 필드 검증 테스트")
  class RequiredFieldsTest {

    @Test
    @DisplayName("모든 필수 필드가 설정된 경우 정상 생성된다")
    void shouldCreateWithAllRequiredFields() {
      // when
      GroupChallengeVerification verification =
          GroupChallengeVerification.builder()
              .participantRecord(participantRecord)
              .imageUrl("https://example.com/image.jpg")
              .content("인증 내용")
              .status(ChallengeStatus.PENDING_APPROVAL)
              .rewarded(false)
              .build();

      // then
      assertThat(verification.getParticipantRecord()).isNotNull();
      assertThat(verification.getImageUrl()).isNotEmpty();
      assertThat(verification.getContent()).isNotEmpty();
      assertThat(verification.getStatus()).isNotNull();
    }
  }
}
