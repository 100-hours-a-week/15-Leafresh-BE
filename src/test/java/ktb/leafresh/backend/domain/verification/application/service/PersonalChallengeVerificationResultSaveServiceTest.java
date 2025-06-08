package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.presentation.dto.request.VerificationResultRequestDto;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class PersonalChallengeVerificationResultSaveServiceTest {

    private VerificationResultProcessor verificationResultProcessor;
    private PersonalChallengeVerificationResultSaveService service;

    @BeforeEach
    void setUp() {
        verificationResultProcessor = mock(VerificationResultProcessor.class);
        service = new PersonalChallengeVerificationResultSaveService(verificationResultProcessor);
    }

    @Test
    @DisplayName("인증 결과를 정상적으로 위임 처리한다")
    void saveResult_success() {
        // given
        Long verificationId = 10L;
        VerificationResultRequestDto dto = VerificationResultRequestDto.builder()
                .type(ChallengeType.PERSONAL)
                .memberId(1L)
                .challengeId(10L)
                .date("2025-06-08")
                .result(true)
                .build();

        // when
        service.saveResult(verificationId, dto);

        // then
        verify(verificationResultProcessor, times(1)).process(verificationId, dto);
    }

    @Test
    @DisplayName("인증 결과 위임 중 예외가 발생해도 propagate된다")
    void saveResult_fail_dueToProcessorException() {
        // given
        Long verificationId = 99L;
        VerificationResultRequestDto dto = VerificationResultRequestDto.builder()
                .type(ChallengeType.PERSONAL)
                .memberId(1L)
                .challengeId(10L)
                .date("2025-06-08")
                .result(false)
                .build();

        doThrow(new RuntimeException("AI 판별 결과 오류"))
                .when(verificationResultProcessor).process(verificationId, dto);

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            service.saveResult(verificationId, dto);
        });

        verify(verificationResultProcessor).process(verificationId, dto);
    }
}
