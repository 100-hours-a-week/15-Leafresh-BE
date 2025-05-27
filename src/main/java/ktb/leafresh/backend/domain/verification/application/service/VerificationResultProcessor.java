package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
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

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationResultProcessor {

    private final GroupChallengeVerificationRepository groupChallengeVerificationRepository;
    private final PersonalChallengeVerificationRepository personalChallengeVerificationRepository;
    private final NotificationCreateService notificationCreateService;
    private final RewardGrantService rewardGrantService;

    public ChallengeStatus getCurrentStatus(Long memberId, Long challengeId, ChallengeType type) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        return switch (type) {
            case GROUP -> groupChallengeVerificationRepository
                    .findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdAndCreatedAtBetween(
                            memberId, challengeId, startOfDay, endOfDay
                    )
                    .map(GroupChallengeVerification::getStatus)
                    .orElse(ChallengeStatus.NOT_SUBMITTED);

            case PERSONAL -> personalChallengeVerificationRepository
                    .findTopByMemberIdAndPersonalChallengeIdAndCreatedAtBetween(
                            memberId, challengeId, startOfDay, endOfDay
                    )
                    .map(PersonalChallengeVerification::getStatus)
                    .orElse(ChallengeStatus.NOT_SUBMITTED);
        };
    }

    public void process(Long verificationId, VerificationResultRequestDto dto) {
        if (dto.type() == ChallengeType.GROUP) {
            processGroup(verificationId, dto);
        } else {
            processPersonal(verificationId, dto);
        }
    }

    private void processGroup(Long verificationId, VerificationResultRequestDto dto) {
        log.info("[Processor 단체 인증 결과 수신] verificationId={}, result={}", verificationId, dto.result());

        GroupChallengeVerification verification = groupChallengeVerificationRepository.findById(verificationId)
                .orElseThrow(() -> {
                    log.error("[Processor 단체 인증 결과 저장 실패] verificationId={} 존재하지 않음", verificationId);
                    throw new CustomException(VerificationErrorCode.VERIFICATION_NOT_FOUND);
                });

        ChallengeStatus newStatus = dto.result() ? ChallengeStatus.SUCCESS : ChallengeStatus.FAILURE;
        verification.markVerified(newStatus);
        log.info("[Processor 상태 업데이트 완료] verificationId={}, newStatus={}", verificationId, newStatus);

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
        log.info("[Processor 알림 생성 완료] memberId={}, challengeTitle={}", member.getId(), challenge.getTitle());

        // 1차 보상: 인증 성공 + 미보상 시 지급
        if (dto.result() && !verification.isRewarded()) {
            int reward = challenge.getLeafReward();
            rewardGrantService.grantLeafPoints(member, reward);
            verification.markRewarded();
            log.info("[Processor 1차 보상 지급 완료] memberId={}, reward={}", member.getId(), reward);
        }

        // 2차 보상: 전체 성공 + 기간 일수만큼 인증 존재 + 미지급 시 지급
        if (record.isAllSuccess()
                && record.getVerifications().size() == challenge.getDurationInDays()
                && !record.hasReceivedParticipationBonus()) {
            rewardGrantService.grantParticipationBonus(member, record);
            record.markParticipationBonusRewarded();
            log.info("[Processor 2차 보너스 지급 완료] memberId={}, bonusGranted=true", member.getId());
        }

        log.info("[Processor 단체 인증 결과 저장 로직 완료] verificationId={}", verificationId);
    }

    private void processPersonal(Long verificationId, VerificationResultRequestDto dto) {
        log.info("[Processor 개인 인증 결과 수신] verificationId={}, type={}, result={}", verificationId, dto.type(), dto.result());

        PersonalChallengeVerification verification = personalChallengeVerificationRepository.findById(verificationId)
                .orElseThrow(() -> {
                    log.error("[Processor 인증 결과 저장 실패] verificationId={}에 해당하는 인증이 존재하지 않음", verificationId);
                    return new CustomException(VerificationErrorCode.VERIFICATION_NOT_FOUND);
                });

        ChallengeStatus newStatus = dto.result() ? ChallengeStatus.SUCCESS : ChallengeStatus.FAILURE;
        verification.markVerified(newStatus);
        log.info("[Processor 인증 상태 업데이트 완료] verificationId={}, newStatus={}", verificationId, newStatus);

        Member member = verification.getMember();
        String challengeTitle = verification.getPersonalChallenge().getTitle();

        log.info("[Processor 알림 생성 시작] memberId={}, challengeTitle={}", member.getId(), challengeTitle);
        notificationCreateService.createChallengeVerificationResultNotification(
                member,
                challengeTitle,
                dto.result(),
                NotificationType.PERSONAL,
                verification.getImageUrl(),
                verification.getPersonalChallenge().getId()
        );
        log.info("[Processor 알림 생성 완료]");

        // 1차 보상: 성공 + 미보상 상태에서만 지급
        if (dto.result()) {
            if (verification.isRewarded()) {
                log.warn("[Processor 보상 스킵] 이미 보상된 인증입니다. verificationId={}, memberId={}", verificationId, member.getId());
            } else {
                int reward = verification.getPersonalChallenge().getLeafReward();
                log.info("[Processor 보상 지급 시작] reward={}, memberId={}", reward, member.getId());

                rewardGrantService.grantLeafPoints(member, reward);
                verification.markRewarded();  // 보상 완료 처리
                log.info("[Processor 보상 지급 완료 및 상태 플래그 업데이트] verificationId={}, memberId={}", verificationId, member.getId());
            }
        }

        log.info("[Processor 개인 인증 결과 저장 및 보상 로직 완료]");
    }
}
