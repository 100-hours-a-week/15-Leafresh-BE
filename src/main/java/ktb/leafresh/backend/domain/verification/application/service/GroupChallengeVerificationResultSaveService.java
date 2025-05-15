package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.member.application.service.RewardGrantService;
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
    private final RewardGrantService rewardGrantService;

    @Transactional
    public void saveResult(Long verificationId, VerificationResultRequestDto dto) {
        log.info("[단체 인증 결과 수신] verificationId={}, result={}", verificationId, dto.result());

        GroupChallengeVerification verification = groupVerificationRepository.findById(verificationId)
                .orElseThrow(() -> {
                    log.error("[단체 인증 결과 저장 실패] verificationId={} 존재하지 않음", verificationId);
                    throw new CustomException(VerificationErrorCode.VERIFICATION_NOT_FOUND);
                });

        ChallengeStatus newStatus = dto.result() ? ChallengeStatus.SUCCESS : ChallengeStatus.FAILURE;
        verification.markVerified(newStatus);
        log.info("[상태 업데이트 완료] verificationId={}, newStatus={}", verificationId, newStatus);

        Member member = verification.getParticipantRecord().getMember();
        GroupChallengeParticipantRecord record = verification.getParticipantRecord();
        GroupChallenge challenge = record.getGroupChallenge();

        notificationCreateService.createChallengeVerificationResultNotification(
                member,
                challenge.getTitle(),
                dto.result(),
                NotificationType.GROUP,
                verification.getImageUrl(),
                challenge.getId()
        );
        log.info("[알림 생성 완료] memberId={}, challengeTitle={}", member.getId(), challenge.getTitle());

        // 1차 보상: 인증 성공 + 미보상 시 지급
        if (dto.result() && !verification.isRewarded()) {
            int reward = challenge.getLeafReward();
            rewardGrantService.grantLeafPoints(member, reward);
            verification.markRewarded();
            log.info("[1차 보상 지급 완료] memberId={}, reward={}", member.getId(), reward);
        }

        // 2차 보상: 전체 성공 + 기간 일수만큼 인증 존재 + 미지급 시 지급
        if (record.isAllSuccess()
                && record.getVerifications().size() == challenge.getDurationInDays()
                && !record.hasReceivedParticipationBonus()) {
            rewardGrantService.grantParticipationBonus(member, record);
            record.markParticipationBonusRewarded();
            log.info("[2차 보너스 지급 완료] memberId={}, bonusGranted=true", member.getId());
        }

        log.info("[단체 인증 결과 저장 로직 완료] verificationId={}", verificationId);
    }
}
