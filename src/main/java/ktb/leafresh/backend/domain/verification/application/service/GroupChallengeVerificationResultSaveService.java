package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.VerificationResultRequestDto;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupChallengeVerificationResultSaveService {

    private final GroupChallengeVerificationRepository groupVerificationRepository;

    @Transactional
    public void saveResult(Long verificationId, VerificationResultRequestDto dto) {
        log.info("[단체 인증 결과 수신] verificationId={}, result={}", verificationId, dto.result());

        GroupChallengeVerification verification = groupVerificationRepository.findById(verificationId)
                .orElseThrow(() -> {
                    log.error("[단체 인증 결과 저장 실패] verificationId={}에 해당하는 인증이 존재하지 않음", verificationId);
                    return new CustomException(VerificationErrorCode.VERIFICATION_NOT_FOUND);
                });

        ChallengeStatus newStatus = dto.result() ? ChallengeStatus.SUCCESS : ChallengeStatus.FAILURE;
        verification.markVerified(newStatus);

        log.info("[단체 인증 결과 저장 완료] verificationId={}, status={}", verificationId, newStatus);
    }
}
