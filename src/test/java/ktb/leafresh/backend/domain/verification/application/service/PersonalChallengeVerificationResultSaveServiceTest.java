package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.presentation.dto.request.VerificationResultRequestDto;
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
    @DisplayName("성공적인 개인 인증 결과가 정상적으로 위임된다")
    void saveResult_success() {
        // given
        Long verificationId = 10L;
        VerificationResultRequestDto dto = VerificationResultRequestDto.builder()
                .type(ChallengeType.PERSONAL)
                .memberId(1L)
                .challengeId(100L)
                .date("2025-06-08")
                .result("true")
                .build();

        // when
        service.saveResult(verificationId, dto);

        // then
        verify(verificationResultProcessor, times(1)).process(verificationId, dto);
    }

    @Test
    @DisplayName("Processor 내부 예외 발생 시 그대로 전파된다")
    void saveResult_throws_whenProcessorFails() {
        // given
        Long verificationId = 99L;
        VerificationResultRequestDto dto = VerificationResultRequestDto.builder()
                .type(ChallengeType.PERSONAL)
                .memberId(2L)
                .challengeId(200L)
                .date("2025-06-08")
                .result("false")
                .build();

        doThrow(new RuntimeException("Mocked Processor Error"))
                .when(verificationResultProcessor).process(verificationId, dto);

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            service.saveResult(verificationId, dto);
        });

        verify(verificationResultProcessor).process(verificationId, dto);
    }

    @Test
    @DisplayName("result 값이 null이더라도 processor에게 위임된다 (isSuccessResult()가 false 처리됨)")
    void saveResult_resultIsNull() {
        // given
        Long verificationId = 777L;
        VerificationResultRequestDto dto = VerificationResultRequestDto.builder()
                .type(ChallengeType.PERSONAL)
                .memberId(3L)
                .challengeId(300L)
                .date("2025-06-08")
                .result(null)
                .build();

        // when
        service.saveResult(verificationId, dto);

        // then
        verify(verificationResultProcessor).process(verificationId, dto);
    }
}
