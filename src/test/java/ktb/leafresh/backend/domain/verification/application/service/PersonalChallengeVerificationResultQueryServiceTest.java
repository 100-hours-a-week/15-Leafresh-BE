package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.domain.entity.PersonalChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.util.polling.ChallengeVerificationPollingExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PersonalChallengeVerificationResultQueryServiceTest {

    private PersonalChallengeVerificationRepository verificationRepository;
    private ChallengeVerificationPollingExecutor pollingExecutor;
    private PersonalChallengeVerificationResultQueryService service;

    @BeforeEach
    void setUp() {
        verificationRepository = mock(PersonalChallengeVerificationRepository.class);
        pollingExecutor = mock(ChallengeVerificationPollingExecutor.class);

        service = new PersonalChallengeVerificationResultQueryService(
                verificationRepository,
                pollingExecutor
        );
    }

    @Test
    @DisplayName("pollingExecutor를 통해 최종 인증 상태를 가져온다")
    void waitForResult_returnsLatestStatus() {
        // given
        when(pollingExecutor.poll(any())).thenReturn(ChallengeStatus.SUCCESS);

        // when
        ChallengeStatus status = service.waitForResult(1L, 10L);

        // then
        assertThat(status).isEqualTo(ChallengeStatus.SUCCESS);
        verify(pollingExecutor, times(1)).poll(any());
    }

    @Test
    @DisplayName("오늘 인증 내역이 없으면 NOT_SUBMITTED를 반환한다")
    void getLatestStatus_noSubmissionToday() {
        // given
        Long memberId = 1L;
        Long challengeId = 10L;

        LocalDateTime now = LocalDateTime.now();
        when(verificationRepository.findTopByMemberIdAndPersonalChallengeIdAndCreatedAtBetween(
                eq(memberId), eq(challengeId),
                any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(Optional.empty());

        // private 메서드 getLatestStatus()는 직접 테스트할 수 없으므로 pollingExecutor 내부 함수 실행을 흉내냅니다
        when(pollingExecutor.poll(any())).thenAnswer(invocation -> {
            var supplier = invocation.getArgument(0);
            return ((java.util.function.Supplier<ChallengeStatus>) supplier).get();
        });

        // when
        ChallengeStatus status = service.waitForResult(memberId, challengeId);

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

        when(pollingExecutor.poll(any())).thenAnswer(invocation -> {
            var supplier = invocation.getArgument(0);
            return ((java.util.function.Supplier<ChallengeStatus>) supplier).get();
        });

        // when
        ChallengeStatus status = service.waitForResult(memberId, challengeId);

        // then
        assertThat(status).isEqualTo(ChallengeStatus.FAILURE);
    }
}
