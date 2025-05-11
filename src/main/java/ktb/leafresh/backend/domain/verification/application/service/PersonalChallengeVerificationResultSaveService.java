package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.notification.application.service.NotificationCreateService;
import ktb.leafresh.backend.domain.notification.domain.entity.enums.NotificationType;
import ktb.leafresh.backend.domain.verification.domain.entity.PersonalChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
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
public class PersonalChallengeVerificationResultSaveService {

    private final PersonalChallengeVerificationRepository verificationRepository;
    private final NotificationCreateService notificationCreateService;

    @Transactional
    public void saveResult(Long verificationId, VerificationResultRequestDto dto) {
        log.info("[AI 인증 결과 수신] verificationId={}, type={}, result={}", verificationId, dto.type(), dto.result());

        PersonalChallengeVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> {
                    log.error("[인증 결과 저장 실패] verificationId={}에 해당하는 인증이 존재하지 않음", verificationId);
                    return new CustomException(VerificationErrorCode.VERIFICATION_NOT_FOUND);
                });

        ChallengeStatus newStatus = dto.result() ? ChallengeStatus.SUCCESS : ChallengeStatus.FAILURE;
        verification.markVerified(newStatus);

        log.info("[인증 결과 저장 완료] verificationId={}, status={}", verificationId, newStatus);

        Member member = verification.getMember();
        String challengeTitle = verification.getPersonalChallenge().getTitle();

        log.info("[알림 생성 시작] memberId={}, challengeTitle={}", member.getId(), challengeTitle);

        notificationCreateService.createChallengeVerificationResultNotification(
                member,
                challengeTitle,
                dto.result(),
                NotificationType.PERSONAL,
                verification.getPersonalChallenge().getId()
        );

        log.info("[알림 생성 완료]");
    }
}
