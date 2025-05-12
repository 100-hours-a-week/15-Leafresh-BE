package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.notification.application.service.NotificationCreateService;
import ktb.leafresh.backend.domain.notification.domain.entity.enums.NotificationType;
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
    private final NotificationCreateService notificationCreateService;

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
        log.info("[단체 인증 상태 업데이트 완료] verificationId={}, newStatus={}", verificationId, newStatus);

        Member member = verification.getParticipantRecord().getMember();
        String challengeTitle = verification.getParticipantRecord().getGroupChallenge().getTitle();

        log.info("[알림 생성 시작] memberId={}, challengeTitle={}", member.getId(), challengeTitle);

        notificationCreateService.createChallengeVerificationResultNotification(
                member,
                challengeTitle,
                dto.result(),
                NotificationType.GROUP,
                verification.getImageUrl(),
                verification.getParticipantRecord().getGroupChallenge().getId()
        );

        log.info("[알림 생성 완료]");
    }
}
