package ktb.leafresh.backend.domain.challenge.group.domain.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeVerificationHistoryResponseDto;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeParticipantRecordFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeVerificationFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GroupChallengeVerificationHistoryCalculator 단위 테스트")
class GroupChallengeVerificationHistoryCalculatorTest {

  private GroupChallengeVerificationHistoryCalculator calculator;

  @BeforeEach
  void setUp() {
    calculator = new GroupChallengeVerificationHistoryCalculator();
  }

  @Test
  @DisplayName("인증 기록이 있는 경우 성공/실패/todayStatus 포함 결과 반환")
  void calculate_withVerifications_returnsResponseDto() {
    // given
    Member member = MemberFixture.of();
    GroupChallengeCategory category = GroupChallengeCategoryFixture.defaultCategory();
    GroupChallenge challenge = GroupChallengeFixture.of(member, category);
    GroupChallengeParticipantRecord participantRecord =
        GroupChallengeParticipantRecordFixture.of(challenge, member);

    GroupChallengeVerification successVerification =
        GroupChallengeVerificationFixture.of(participantRecord, ChallengeStatus.SUCCESS);
    GroupChallengeVerification failureVerification =
        GroupChallengeVerificationFixture.of(participantRecord, ChallengeStatus.FAILURE);
    List<GroupChallengeVerification> verifications =
        List.of(successVerification, failureVerification);

    // 오늘 날짜 기준 설정
    LocalDate todayKST = LocalDate.now(ZoneId.of("Asia/Seoul"));
    LocalDateTime startDate = todayKST.atStartOfDay(); // 자정
    LocalDateTime endDate = todayKST.plusDays(6).atTime(23, 59);

    ReflectionTestUtils.setField(challenge, "startDate", startDate);
    ReflectionTestUtils.setField(challenge, "endDate", endDate);

    LocalDateTime baseTime =
        todayKST
            .atTime(12, 0)
            .atZone(ZoneId.of("Asia/Seoul"))
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime();
    ReflectionTestUtils.setField(successVerification, "createdAt", baseTime.plusMinutes(1));
    ReflectionTestUtils.setField(failureVerification, "createdAt", baseTime);

    // when
    GroupChallengeVerificationHistoryResponseDto result =
        calculator.calculate(challenge, participantRecord, verifications);

    // then
    assertThat(result.id()).isEqualTo(challenge.getId());
    assertThat(result.title()).isEqualTo(challenge.getTitle());

    GroupChallengeVerificationHistoryResponseDto.AchievementDto achievement = result.achievement();
    assertThat(achievement.success()).isEqualTo(1);
    assertThat(achievement.failure()).isEqualTo(1);
    assertThat(achievement.remaining()).isEqualTo(8);

    assertThat(result.verifications()).hasSize(2);
    assertThat(result.verifications()).extracting("day").containsOnly(1);
    assertThat(result.verifications())
        .extracting("status")
        .containsExactly(ChallengeStatus.SUCCESS, ChallengeStatus.FAILURE);

    assertThat(result.todayStatus()).isEqualTo("DONE");
  }

  @Test
  @DisplayName("인증 기록이 없을 경우 todayStatus는 NOT_SUBMITTED이고 remaining 계산됨")
  void calculate_withoutVerifications_returnsEmptyListAndNotSubmitted() {
    // given
    Member member = MemberFixture.of();
    GroupChallengeCategory category = GroupChallengeCategoryFixture.defaultCategory();
    GroupChallenge challenge = GroupChallengeFixture.of(member, category);
    GroupChallengeParticipantRecord participantRecord =
        GroupChallengeParticipantRecordFixture.of(challenge, member);

    List<GroupChallengeVerification> verifications = List.of();

    // when
    GroupChallengeVerificationHistoryResponseDto result =
        calculator.calculate(challenge, participantRecord, verifications);

    // then
    assertThat(result.verifications()).isEmpty();
    assertThat(result.todayStatus()).isEqualTo("NOT_SUBMITTED");

    GroupChallengeVerificationHistoryResponseDto.AchievementDto achievement = result.achievement();
    assertThat(achievement.success()).isZero();
    assertThat(achievement.failure()).isZero();
    assertThat(achievement.remaining()).isGreaterThanOrEqualTo(0);
  }

  @Test
  @DisplayName("오늘 인증 상태가 PENDING_APPROVAL인 경우 todayStatus는 PENDING_APPROVAL")
  void calculate_todayVerificationPendingApproval_returnsPending() {
    // given
    Member member = MemberFixture.of();
    GroupChallengeCategory category = GroupChallengeCategoryFixture.defaultCategory();
    GroupChallenge challenge = GroupChallengeFixture.of(member, category);
    GroupChallengeParticipantRecord participantRecord =
        GroupChallengeParticipantRecordFixture.of(challenge, member);

    GroupChallengeVerification pendingVerification =
        GroupChallengeVerificationFixture.of(participantRecord, ChallengeStatus.PENDING_APPROVAL);

    // createdAt을 오늘 날짜로 맞춤 (KST 기준 12:00)
    LocalDate todayKST = LocalDate.now(ZoneId.of("Asia/Seoul"));
    LocalDateTime createdAtUTC =
        todayKST
            .atTime(12, 0)
            .atZone(ZoneId.of("Asia/Seoul"))
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime();
    ReflectionTestUtils.setField(pendingVerification, "createdAt", createdAtUTC);

    List<GroupChallengeVerification> verifications = List.of(pendingVerification);

    // when
    GroupChallengeVerificationHistoryResponseDto result =
        calculator.calculate(challenge, participantRecord, verifications);

    // then
    assertThat(result.todayStatus()).isEqualTo("PENDING_APPROVAL");
  }
}
