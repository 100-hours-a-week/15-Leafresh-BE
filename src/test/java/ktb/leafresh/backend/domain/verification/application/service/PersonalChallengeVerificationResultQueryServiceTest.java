package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.domain.entity.PersonalChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PersonalChallengeVerificationResultQueryServiceTest {

  @Mock private PersonalChallengeVerificationRepository verificationRepository;

  @InjectMocks private PersonalChallengeVerificationResultQueryService queryService;

  private final Long memberId = 1L;
  private final Long challengeId = 2L;

  private LocalDateTime fixedKstStart;
  private LocalDateTime fixedKstEnd;
  private LocalDateTime fixedUtcStart;
  private LocalDateTime fixedUtcEnd;

  @BeforeEach
  void setUp() {
    ZoneId kst = ZoneId.of("Asia/Seoul");
    LocalDate today = LocalDate.now(kst);

    fixedKstStart = today.atStartOfDay();
    fixedKstEnd = today.atTime(23, 59, 59);

    fixedUtcStart = fixedKstStart.atZone(kst).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    fixedUtcEnd = fixedKstEnd.atZone(kst).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
  }

  @Test
  @DisplayName("오늘 인증 기록이 있는 경우 - 해당 인증의 상태 반환")
  void getLatestStatus_withVerification_returnsStatus() {
    // given
    PersonalChallengeVerification verification =
        PersonalChallengeVerification.builder().status(ChallengeStatus.SUCCESS).build();
    ReflectionTestUtils.setField(verification, "createdAt", fixedUtcStart.plusHours(2));

    given(
            verificationRepository.findTopByMemberIdAndPersonalChallengeIdAndCreatedAtBetween(
                memberId, challengeId, fixedUtcStart, fixedUtcEnd))
        .willReturn(Optional.of(verification));

    // when
    ChallengeStatus result = queryService.getLatestStatus(memberId, challengeId);

    // then
    assertThat(result).isEqualTo(ChallengeStatus.SUCCESS);
  }

  @Test
  @DisplayName("오늘 인증 기록이 없는 경우 - NOT_SUBMITTED 반환")
  void getLatestStatus_noVerification_returnsNotSubmitted() {
    // given
    given(
            verificationRepository.findTopByMemberIdAndPersonalChallengeIdAndCreatedAtBetween(
                memberId, challengeId, fixedUtcStart, fixedUtcEnd))
        .willReturn(Optional.empty());

    // when
    ChallengeStatus result = queryService.getLatestStatus(memberId, challengeId);

    // then
    assertThat(result).isEqualTo(ChallengeStatus.NOT_SUBMITTED);
  }
}
