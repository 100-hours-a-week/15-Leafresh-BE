package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.domain.entity.PersonalChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PersonalChallengeVerificationResultQueryServiceTest {

    private PersonalChallengeVerificationRepository verificationRepository;
    private PersonalChallengeVerificationResultQueryService service;

    @BeforeEach
    void setUp() {
        verificationRepository = mock(PersonalChallengeVerificationRepository.class);
        service = new PersonalChallengeVerificationResultQueryService(verificationRepository);
    }

    @Test
    @DisplayName("오늘 인증 내역이 없으면 NOT_SUBMITTED를 반환한다")
    void getLatestStatus_noSubmissionToday() {
        // given
        Long memberId = 1L;
        Long challengeId = 10L;

        when(verificationRepository.findTopByMemberIdAndPersonalChallengeIdAndCreatedAtBetween(
                eq(memberId), eq(challengeId),
                any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(Optional.empty());

        // when
        ChallengeStatus status = service.getLatestStatus(memberId, challengeId);

        // then
        assertThat(status).isEqualTo(ChallengeStatus.NOT_SUBMITTED);
    }

    @Test
    @DisplayName("오늘 인증 내역이 있으면 해당 status를 반환한다")
    void getLatestStatus_submissionFound() {
        // given
        Long memberId = 1L;
        Long challengeId = 10L;

        var verification = mock(PersonalChallengeVerification.class);
        when(verification.getStatus()).thenReturn(ChallengeStatus.FAILURE);

        when(verificationRepository.findTopByMemberIdAndPersonalChallengeIdAndCreatedAtBetween(
                eq(memberId), eq(challengeId),
                any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(Optional.of(verification));

        // when
        ChallengeStatus status = service.getLatestStatus(memberId, challengeId);

        // then
        assertThat(status).isEqualTo(ChallengeStatus.FAILURE);
    }
}
