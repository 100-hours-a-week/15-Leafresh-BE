package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.presentation.dto.request.VerificationResultRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonalChallengeVerificationResultSaveService {

  private final VerificationResultProcessor verificationResultProcessor;

  @Transactional
  public void saveResult(Long verificationId, VerificationResultRequestDto dto) {
    log.info("[개인 인증 결과 위임 시작] verificationId={}, result={}", verificationId, dto.result());
    verificationResultProcessor.process(verificationId, dto);
    log.info("[개인 인증 결과 위임 완료] verificationId={}", verificationId);
  }
}
