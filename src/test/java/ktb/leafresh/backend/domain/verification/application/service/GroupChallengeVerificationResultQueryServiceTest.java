package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.support.fixture.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GroupChallengeVerificationResultQueryServiceTest {

  @Mock private GroupChallengeVerificationRepository verificationRepository;

  @InjectMocks private GroupChallengeVerificationResultQueryService resultQueryService;

  private Member member;
  private GroupChallenge challenge;
  private GroupChallengeParticipantRecord participantRecord;

  @BeforeEach
  void setUp() {
    member = MemberFixture.of();
    GroupChallengeCategory category = GroupChallengeCategoryFixture.defaultCategory();
    challenge = GroupChallengeFixture.of(member, category);
    participantRecord = GroupChallengeParticipantRecordFixture.of(challenge, member);
  }

  @Test
  @DisplayName("오늘 날짜에 성공 인증이 있으면 SUCCESS 상태를 반환한다")
  void getLatestStatus_withSuccessVerification_returnsSuccess() {
    // given
    GroupChallengeVerification verification =
        GroupChallengeVerificationFixture.of(participantRecord, ChallengeStatus.SUCCESS);
    Long memberId = member.getId();
    Long challengeId = challenge.getId();

    ZoneId kst = ZoneId.of("Asia/Seoul");
    LocalDateTime startKst = LocalDate.now(kst).atStartOfDay();
    LocalDateTime endKst = LocalDate.now(kst).atTime(23, 59, 59);
    LocalDateTime startUtc =
        startKst.atZone(kst).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    LocalDateTime endUtc = endKst.atZone(kst).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

    given(
            verificationRepository
                .findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdAndCreatedAtBetween(
                    memberId, challengeId, startUtc, endUtc))
        .willReturn(Optional.of(verification));

    // when
    ChallengeStatus result = resultQueryService.getLatestStatus(memberId, challengeId);

    // then
    assertThat(result).isEqualTo(ChallengeStatus.SUCCESS);
  }

  @Test
  @DisplayName("오늘 날짜에 인증이 없으면 NOT_SUBMITTED 상태를 반환한다")
  void getLatestStatus_withNoVerification_returnsNotSubmitted() {
    // given
    Long memberId = member.getId();
    Long challengeId = challenge.getId();

    ZoneId kst = ZoneId.of("Asia/Seoul");
    LocalDateTime startKst = LocalDate.now(kst).atStartOfDay();
    LocalDateTime endKst = LocalDate.now(kst).atTime(23, 59, 59);
    LocalDateTime startUtc =
        startKst.atZone(kst).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    LocalDateTime endUtc = endKst.atZone(kst).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

    given(
            verificationRepository
                .findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdAndCreatedAtBetween(
                    memberId, challengeId, startUtc, endUtc))
        .willReturn(Optional.empty());

    // when
    ChallengeStatus result = resultQueryService.getLatestStatus(memberId, challengeId);

    // then
    assertThat(result).isEqualTo(ChallengeStatus.NOT_SUBMITTED);
  }
}
