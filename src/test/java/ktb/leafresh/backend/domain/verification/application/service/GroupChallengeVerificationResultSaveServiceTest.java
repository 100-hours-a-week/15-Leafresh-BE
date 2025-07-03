package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.presentation.dto.request.VerificationResultRequestDto;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupChallengeVerificationResultSaveServiceTest {

    @Mock
    private VerificationResultProcessor verificationResultProcessor;

    @InjectMocks
    private GroupChallengeVerificationResultSaveService saveService;

    @Test
    @DisplayName("단체 인증 결과 저장 요청 시, Processor로 위임된다")
    void saveResult_delegatesToProcessor() {
        // given
        Long verificationId = 1L;

        VerificationResultRequestDto request = VerificationResultRequestDto.builder()
                .type(ChallengeType.GROUP)
                .memberId(10L)
                .challengeId(100L)
                .verificationId(verificationId)
                .date("2024-01-01")
                .result("true")
                .build();

        // when
        saveService.saveResult(verificationId, request);

        // then
        verify(verificationResultProcessor, times(1)).process(verificationId, request);
    }
}
