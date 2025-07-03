package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.member.application.service.BadgeGrantManager;
import ktb.leafresh.backend.domain.member.application.service.RewardGrantService;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.notification.application.service.NotificationCreateService;
import ktb.leafresh.backend.domain.notification.domain.entity.enums.NotificationType;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.domain.entity.PersonalChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.VerificationResultRequestDto;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeType;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationResultProcessor {

    private final GroupChallengeVerificationRepository groupChallengeVerificationRepository;
    private final PersonalChallengeVerificationRepository personalChallengeVerificationRepository;
    private final NotificationCreateService notificationCreateService;
    private final RewardGrantService rewardGrantService;
    private final BadgeGrantManager badgeGrantManager;

    @Transactional
    public void process(Long verificationId, VerificationResultRequestDto dto) {
        if (!dto.isSuccessResult()) {
            log.warn("[Processor 인증 결과 무시] 정상적인 true/false 값이 아님: result={}, type={}", dto.result(), dto.type());
            return;
        }

        if (dto.type() == ChallengeType.GROUP) {
            processGroup(verificationId, dto);
        } else {
            processPersonal(verificationId, dto);
        }
    }

    private void processGroup(Long verificationId, VerificationResultRequestDto dto) {
        boolean isSuccess = dto.resultAsBoolean();
        log.info("[Processor 단체 인증 결과 수신] verificationId={}, result={}", verificationId, isSuccess);

        GroupChallengeVerification verification = groupChallengeVerificationRepository.findById(verificationId)
                .orElseThrow(() -> {
                    log.error("[Processor 단체 인증 결과 저장 실패] verificationId={} 존재하지 않음", verificationId);
                    throw new CustomException(VerificationErrorCode.VERIFICATION_NOT_FOUND);
                });

        ChallengeStatus newStatus = isSuccess ? ChallengeStatus.SUCCESS : ChallengeStatus.FAILURE;
        verification.markVerified(newStatus);
        log.info("[Processor 상태 업데이트 완료] verificationId={}, newStatus={}", verificationId, newStatus);

        Member member = verification.getParticipantRecord().getMember();
        GroupChallengeParticipantRecord record = verification.getParticipantRecord();
        GroupChallenge challenge = record.getGroupChallenge();

        notificationCreateService.createChallengeVerificationResultNotification(
                member,
                challenge.getTitle(),
                isSuccess,
                NotificationType.GROUP,
                verification.getImageUrl(),
                challenge.getId()
        );
        log.info("[Processor 알림 생성 완료] memberId={}, challengeTitle={}", member.getId(), challenge.getTitle());

        if (isSuccess && !verification.isRewarded()) {
            int reward = challenge.getLeafReward();
            rewardGrantService.grantLeafPoints(member, reward);
            verification.markRewarded();
            log.info("[Processor 1차 보상 지급 완료] memberId={}, reward={}", member.getId(), reward);
        }

        if (record.isAllSuccess()
                && record.getVerifications().size() == challenge.getDurationInDays()
                && !record.hasReceivedParticipationBonus()) {
            rewardGrantService.grantParticipationBonus(member, record);
            record.markParticipationBonusRewarded();
            log.info("[Processor 2차 보너스 지급 완료] memberId={}, bonusGranted=true", member.getId());
        }

        badgeGrantManager.evaluateAllAndGrant(member);
        log.info("[Processor 단체 인증 결과 저장 로직 완료] verificationId={}", verificationId);
    }

    private void processPersonal(Long verificationId, VerificationResultRequestDto dto) {
        boolean isSuccess = dto.resultAsBoolean();
        log.info("[Processor 개인 인증 결과 수신] verificationId={}, type={}, result={}", verificationId, dto.type(), isSuccess);

        PersonalChallengeVerification verification = personalChallengeVerificationRepository.findById(verificationId)
                .orElseThrow(() -> {
                    log.error("[Processor 인증 결과 저장 실패] verificationId={}에 해당하는 인증이 존재하지 않음", verificationId);
                    return new CustomException(VerificationErrorCode.VERIFICATION_NOT_FOUND);
                });

        ChallengeStatus newStatus = isSuccess ? ChallengeStatus.SUCCESS : ChallengeStatus.FAILURE;
        verification.markVerified(newStatus);
        log.info("[Processor 인증 상태 업데이트 완료] verificationId={}, newStatus={}", verificationId, newStatus);

        Member member = verification.getMember();
        String challengeTitle = verification.getPersonalChallenge().getTitle();

        notificationCreateService.createChallengeVerificationResultNotification(
                member,
                challengeTitle,
                isSuccess,
                NotificationType.PERSONAL,
                verification.getImageUrl(),
                verification.getPersonalChallenge().getId()
        );
        log.info("[Processor 알림 생성 완료] memberId={}, challengeTitle={}", member.getId(), challengeTitle);

        if (isSuccess) {
            if (verification.isRewarded()) {
                log.warn("[Processor 보상 스킵] 이미 보상된 인증입니다. verificationId={}, memberId={}", verificationId, member.getId());
            } else {
                int reward = verification.getPersonalChallenge().getLeafReward();
                rewardGrantService.grantLeafPoints(member, reward);
                verification.markRewarded();
                log.info("[Processor 보상 지급 완료] verificationId={}, memberId={}, reward={}", verificationId, member.getId(), reward);
            }
        }

        badgeGrantManager.evaluateAllAndGrant(member);
        log.info("[Processor 개인 인증 결과 저장 및 보상 로직 완료]");
    }
}
