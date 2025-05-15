package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.member.application.service.RewardGrantService;
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
    private final RewardGrantService rewardGrantService;

    @Transactional
    public void saveResult(Long verificationId, VerificationResultRequestDto dto) {
        log.info("[개인 인증 결과 수신] verificationId={}, type={}, result={}", verificationId, dto.type(), dto.result());

        PersonalChallengeVerification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> {
                    log.error("[인증 결과 저장 실패] verificationId={}에 해당하는 인증이 존재하지 않음", verificationId);
                    return new CustomException(VerificationErrorCode.VERIFICATION_NOT_FOUND);
                });

        ChallengeStatus newStatus = dto.result() ? ChallengeStatus.SUCCESS : ChallengeStatus.FAILURE;
        verification.markVerified(newStatus);
        log.info("[인증 상태 업데이트 완료] verificationId={}, newStatus={}", verificationId, newStatus);

        Member member = verification.getMember();
        String challengeTitle = verification.getPersonalChallenge().getTitle();

        log.info("[알림 생성 시작] memberId={}, challengeTitle={}", member.getId(), challengeTitle);
        notificationCreateService.createChallengeVerificationResultNotification(
                member,
                challengeTitle,
                dto.result(),
                NotificationType.PERSONAL,
                verification.getImageUrl(),
                verification.getPersonalChallenge().getId()
        );
        log.info("[알림 생성 완료]");

        // 1차 보상: 성공 + 미보상 상태에서만 지급
        if (dto.result()) {
            if (verification.isRewarded()) {
                log.warn("[보상 스킵] 이미 보상된 인증입니다. verificationId={}, memberId={}", verificationId, member.getId());
            } else {
                int reward = verification.getPersonalChallenge().getLeafReward();
                log.info("[보상 지급 시작] reward={}, memberId={}", reward, member.getId());

                rewardGrantService.grantLeafPoints(member, reward);
                verification.markRewarded();  // 보상 완료 처리
                log.info("[보상 지급 완료 및 상태 플래그 업데이트] verificationId={}, memberId={}", verificationId, member.getId());
            }
        }

        log.info("[개인 인증 결과 저장 및 보상 로직 완료]");
    }
}
