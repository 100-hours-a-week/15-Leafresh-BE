package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeParticipantRecordRepository;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.domain.event.VerificationCreatedEvent;
import ktb.leafresh.backend.domain.verification.infrastructure.dto.request.AiVerificationRequestDto;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.GroupChallengeVerificationRequestDto;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeType;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Service
public class GroupChallengeVerificationSubmitService {

    private final GroupChallengeRepository groupChallengeRepository;
    private final GroupChallengeParticipantRecordRepository recordRepository;
    private final GroupChallengeVerificationRepository verificationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void submit(Long memberId, Long challengeId, GroupChallengeVerificationRequestDto dto) {
        GroupChallenge challenge = groupChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_NOT_FOUND));

        GroupChallengeParticipantRecord record = recordRepository
                .findByGroupChallengeIdAndMemberIdAndDeletedAtIsNull(challengeId, memberId)
                .orElseThrow(() -> new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_RECORD_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        boolean alreadySubmitted = verificationRepository
                .findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdOrderByCreatedAtDesc(
                        memberId, challengeId)
                .filter(v -> v.getCreatedAt().toLocalDate().equals(now.toLocalDate()))
                .isPresent();

        if (alreadySubmitted) {
            throw new CustomException(VerificationErrorCode.ALREADY_SUBMITTED);
        }

        GroupChallengeVerification verification = GroupChallengeVerification.builder()
                .participantRecord(record)
                .imageUrl(dto.imageUrl())
                .content(dto.content())
                .status(ChallengeStatus.PENDING_APPROVAL)
                .build();

        verificationRepository.save(verification);

        // 이벤트 발행 (커밋 후 실행됨)
        AiVerificationRequestDto aiRequest = AiVerificationRequestDto.builder()
                .verificationId(verification.getId())
                .type(ChallengeType.GROUP)
                .imageUrl(dto.imageUrl())
                .memberId(memberId)
                .challengeId(challengeId)
                .date(now.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .challengeName(challenge.getTitle())
                .build();

        eventPublisher.publishEvent(new VerificationCreatedEvent(aiRequest));
    }
}
