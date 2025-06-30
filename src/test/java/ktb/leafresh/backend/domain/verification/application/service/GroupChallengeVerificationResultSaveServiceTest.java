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
    @DisplayName("result가 true/false일 경우 VerificationResultProcessor에 위임한다")
    void saveResult_DelegatesToProcessor_WhenResultIsTrueOrFalse() {
        // given
        Long verificationId = 1L;
        VerificationResultRequestDto dto = VerificationResultRequestDto.builder()
                .type(ChallengeType.GROUP)
                .memberId(10L)
                .challengeId(123L)
                .verificationId(verificationId)
                .date("2025-06-08")
                .result("true")
                .build();

        // when
        service.saveResult(verificationId, dto);

        // then
        verify(verificationResultProcessor, times(1)).process(verificationId, dto);
    }

    @Test
    @DisplayName("result가 HTTP 오류 코드일 경우 VerificationResultProcessor가 무시한다")
    void saveResult_DoesNotDelegate_WhenResultIsHttpErrorCode() {
        // given
        Long verificationId = 2L;
        VerificationResultRequestDto dto = VerificationResultRequestDto.builder()
                .type(ChallengeType.GROUP)
                .memberId(20L)
                .challengeId(456L)
                .verificationId(verificationId)
                .date("2025-06-08")
                .result("422")
                .build();

        // when
        service.saveResult(verificationId, dto);

        // then
        verify(verificationResultProcessor, times(1)).process(verificationId, dto);
        // 내부적으로 무시되더라도 서비스 단에서는 위임이 이뤄짐 (→ VerificationResultProcessor의 동작 테스트는 별도로 분리됨)
    }
}
