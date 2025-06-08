package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.presentation.dto.request.VerificationResultRequestDto;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class GroupChallengeVerificationResultSaveServiceTest {

    private VerificationResultProcessor verificationResultProcessor;
    private GroupChallengeVerificationResultSaveService service;

    @BeforeEach
    void setUp() {
        verificationResultProcessor = mock(VerificationResultProcessor.class);
        service = new GroupChallengeVerificationResultSaveService(verificationResultProcessor);
    }

    @Test
    @DisplayName("단체 인증 결과를 VerificationResultProcessor에 위임한다")
    void saveResult_DelegatesToProcessor() {
        // given
        Long verificationId = 1L;

        VerificationResultRequestDto dto = VerificationResultRequestDto.builder()
                .type(ChallengeType.GROUP)
                .memberId(10L)
                .challengeId(123L)
                .date("2025-06-08")
                .result(true)
                .build();

        // when
        service.saveResult(verificationId, dto);

        // then
        verify(verificationResultProcessor, times(1)).process(verificationId, dto);
    }
}
