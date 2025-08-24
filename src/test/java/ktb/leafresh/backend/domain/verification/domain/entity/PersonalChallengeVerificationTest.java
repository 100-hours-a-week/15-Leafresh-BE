package ktb.leafresh.backend.domain.verification.domain.entity;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import ktb.leafresh.backend.support.fixture.PersonalChallengeFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PersonalChallengeVerification 엔티티 테스트")
class PersonalChallengeVerificationTest {

  @Nested
  @DisplayName("생성 테스트")
  class CreationTest {

    @Test
    @DisplayName("빌더를 통해 정상적으로 생성된다")
    void shouldCreateSuccessfully() {
      // given
      Member member = MemberFixture.of();
      PersonalChallenge personalChallenge = PersonalChallengeFixture.of();
      LocalDateTime joinedAt = LocalDateTime.of(2025, 1, 1, 10, 0);
      String imageUrl = "https://example.com/image.jpg";
      String content = "개인 챌린지 인증 내용";
      ChallengeStatus status = ChallengeStatus.PENDING_APPROVAL;
      boolean rewarded = false;

      // when
      PersonalChallengeVerification verification =
          PersonalChallengeVerification.builder()
              .member(member)
              .personalChallenge(personalChallenge)
              .joinedAt(joinedAt)
              .imageUrl(imageUrl)
              .content(content)
              .status(status)
              .rewarded(rewarded)
              .build();

      // then
      assertThat(verification.getMember()).isEqualTo(member);
      assertThat(verification.getPersonalChallenge()).isEqualTo(personalChallenge);
      assertThat(verification.getJoinedAt()).isEqualTo(joinedAt);
      assertThat(verification.getImageUrl()).isEqualTo(imageUrl);
      assertThat(verification.getContent()).isEqualTo(content);
      assertThat(verification.getStatus()).isEqualTo(status);
      assertThat(verification.isRewarded()).isEqualTo(rewarded);
    }
  }

  @Nested
  @DisplayName("prePersist 테스트")
  class PrePersistTest {

    @Test
    @DisplayName("저장 전 리워드 기본값이 false로 설정된다")
    void shouldSetRewardedToFalseOnPrePersist() {
      // given
      PersonalChallengeVerification verification =
          PersonalChallengeVerification.builder()
              .member(MemberFixture.of())
              .personalChallenge(PersonalChallengeFixture.of())
              .joinedAt(LocalDateTime.of(2025, 1, 1, 10, 0))
              .imageUrl("https://example.com/image.jpg")
              .content("인증 내용")
              .status(ChallengeStatus.PENDING_APPROVAL)
              .rewarded(true) // 의도적으로 true로 설정
              .build();

      // when
      verification.prePersist();

      // then
      assertThat(verification.isRewarded()).isFalse();
    }
  }

  @Nested
  @DisplayName("인증 완료 처리 테스트")
  class MarkVerifiedTest {

    @Test
    @DisplayName("성공 상태로 인증 완료 처리가 된다")
    void shouldMarkVerifiedWithSuccessStatus() {
      // given
      PersonalChallengeVerification verification =
          PersonalChallengeVerification.builder()
              .member(MemberFixture.of())
              .personalChallenge(PersonalChallengeFixture.of())
              .joinedAt(LocalDateTime.of(2025, 1, 1, 10, 0))
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
      PersonalChallengeVerification verification =
          PersonalChallengeVerification.builder()
              .member(MemberFixture.of())
              .personalChallenge(PersonalChallengeFixture.of())
              .joinedAt(LocalDateTime.of(2025, 1, 1, 10, 0))
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
      PersonalChallengeVerification verification =
          PersonalChallengeVerification.builder()
              .member(MemberFixture.of())
              .personalChallenge(PersonalChallengeFixture.of())
              .joinedAt(LocalDateTime.of(2025, 1, 1, 10, 0))
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
      PersonalChallengeVerification verification =
          PersonalChallengeVerification.builder()
              .member(MemberFixture.of())
              .personalChallenge(PersonalChallengeFixture.of())
              .joinedAt(LocalDateTime.of(2025, 1, 1, 10, 0))
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
      PersonalChallengeVerification verification =
          PersonalChallengeVerification.builder()
              .member(MemberFixture.of())
              .personalChallenge(PersonalChallengeFixture.of())
              .joinedAt(LocalDateTime.of(2025, 1, 1, 10, 0))
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
  @DisplayName("연관 관계 테스트")
  class RelationshipTest {

    @Test
    @DisplayName("회원과의 연관 관계가 정상적으로 설정된다")
    void shouldSetMemberRelationship() {
      // given
      Member member = MemberFixture.of("test@example.com", "테스터");
      PersonalChallenge personalChallenge = PersonalChallengeFixture.of();

      // when
      PersonalChallengeVerification verification =
          PersonalChallengeVerification.builder()
              .member(member)
              .personalChallenge(personalChallenge)
              .joinedAt(LocalDateTime.of(2025, 1, 1, 10, 0))
              .imageUrl("https://example.com/image.jpg")
              .content("인증 내용")
              .status(ChallengeStatus.SUCCESS)
              .rewarded(false)
              .build();

      // then
      assertThat(verification.getMember()).isEqualTo(member);
      assertThat(verification.getMember().getEmail()).isEqualTo("test@example.com");
      assertThat(verification.getMember().getNickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("개인 챌린지와의 연관 관계가 정상적으로 설정된다")
    void shouldSetPersonalChallengeRelationship() {
      // given
      Member member = MemberFixture.of();
      PersonalChallenge personalChallenge = PersonalChallengeFixture.of();

      // when
      PersonalChallengeVerification verification =
          PersonalChallengeVerification.builder()
              .member(member)
              .personalChallenge(personalChallenge)
              .joinedAt(LocalDateTime.of(2025, 1, 1, 10, 0))
              .imageUrl("https://example.com/image.jpg")
              .content("인증 내용")
              .status(ChallengeStatus.SUCCESS)
              .rewarded(false)
              .build();

      // then
      assertThat(verification.getPersonalChallenge()).isEqualTo(personalChallenge);
    }
  }

  @Nested
  @DisplayName("필수 필드 검증 테스트")
  class RequiredFieldsTest {

    @Test
    @DisplayName("모든 필수 필드가 설정된 경우 정상 생성된다")
    void shouldCreateWithAllRequiredFields() {
      // given & when
      PersonalChallengeVerification verification =
          PersonalChallengeVerification.builder()
              .member(MemberFixture.of())
              .personalChallenge(PersonalChallengeFixture.of())
              .joinedAt(LocalDateTime.of(2025, 1, 1, 10, 0))
              .imageUrl("https://example.com/image.jpg")
              .content("인증 내용")
              .status(ChallengeStatus.PENDING_APPROVAL)
              .rewarded(false)
              .build();

      // then
      assertThat(verification.getMember()).isNotNull();
      assertThat(verification.getPersonalChallenge()).isNotNull();
      assertThat(verification.getImageUrl()).isNotEmpty();
      assertThat(verification.getContent()).isNotEmpty();
      assertThat(verification.getStatus()).isNotNull();
    }
  }

  @Nested
  @DisplayName("시간 관련 필드 테스트")
  class TimeFieldsTest {

    @Test
    @DisplayName("joinedAt과 verifiedAt이 독립적으로 관리된다")
    void shouldManageTimeFieldsIndependently() {
      // given
      LocalDateTime joinedAt = LocalDateTime.of(2025, 1, 1, 10, 0);
      PersonalChallengeVerification verification =
          PersonalChallengeVerification.builder()
              .member(MemberFixture.of())
              .personalChallenge(PersonalChallengeFixture.of())
              .joinedAt(joinedAt)
              .imageUrl("https://example.com/image.jpg")
              .content("인증 내용")
              .status(ChallengeStatus.PENDING_APPROVAL)
              .rewarded(false)
              .build();

      LocalDateTime beforeVerification = LocalDateTime.now().minusSeconds(1);

      // when
      verification.markVerified(ChallengeStatus.SUCCESS);

      // then
      LocalDateTime afterVerification = LocalDateTime.now().plusSeconds(1);
      assertThat(verification.getJoinedAt()).isEqualTo(joinedAt);
      assertThat(verification.getVerifiedAt()).isBetween(beforeVerification, afterVerification);
      assertThat(verification.getVerifiedAt()).isAfter(verification.getJoinedAt());
    }
  }

  @Nested
  @DisplayName("상태 변화 테스트")
  class StatusChangeTest {

    @Test
    @DisplayName("PENDING_APPROVAL에서 SUCCESS로 상태 변경")
    void shouldChangeFromPendingToSuccess() {
      // given
      PersonalChallengeVerification verification =
          PersonalChallengeVerification.builder()
              .member(MemberFixture.of())
              .personalChallenge(PersonalChallengeFixture.of())
              .joinedAt(LocalDateTime.of(2025, 1, 1, 10, 0))
              .imageUrl("https://example.com/image.jpg")
              .content("인증 내용")
              .status(ChallengeStatus.PENDING_APPROVAL)
              .rewarded(false)
              .build();

      // when
      verification.markVerified(ChallengeStatus.SUCCESS);

      // then
      assertThat(verification.getStatus()).isEqualTo(ChallengeStatus.SUCCESS);
      assertThat(verification.getVerifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("PENDING_APPROVAL에서 FAILURE로 상태 변경")
    void shouldChangeFromPendingToFailure() {
      // given
      PersonalChallengeVerification verification =
          PersonalChallengeVerification.builder()
              .member(MemberFixture.of())
              .personalChallenge(PersonalChallengeFixture.of())
              .joinedAt(LocalDateTime.of(2025, 1, 1, 10, 0))
              .imageUrl("https://example.com/image.jpg")
              .content("인증 내용")
              .status(ChallengeStatus.PENDING_APPROVAL)
              .rewarded(false)
              .build();

      // when
      verification.markVerified(ChallengeStatus.FAILURE);

      // then
      assertThat(verification.getStatus()).isEqualTo(ChallengeStatus.FAILURE);
      assertThat(verification.getVerifiedAt()).isNotNull();
    }
  }
}
