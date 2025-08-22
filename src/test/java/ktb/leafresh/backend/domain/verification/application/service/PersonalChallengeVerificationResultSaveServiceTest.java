package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.presentation.dto.request.VerificationResultRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonalChallengeVerificationResultSaveServiceTest {

  @Mock private VerificationResultProcessor verificationResultProcessor;

  @InjectMocks private PersonalChallengeVerificationResultSaveService saveService;

  @Test
  @DisplayName("개인 인증 결과 저장 요청 시 - Processor가 위임 호출됨")
  void saveResult_callsProcessor() {
    // given
    Long verificationId = 1L;
    VerificationResultRequestDto dto = mock(VerificationResultRequestDto.class);

    // when
    saveService.saveResult(verificationId, dto);

    // then
    verify(verificationResultProcessor, times(1)).process(verificationId, dto);
  }
}
