package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeParticipantRecordRepository;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.client.AiGroupChallengeVerificationClient;
import ktb.leafresh.backend.domain.verification.infrastructure.dto.request.AiGroupChallengeVerificationRequestDto;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.GroupChallengeVerificationRequestDto;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class GroupChallengeVerificationSubmitService {

    private final GroupChallengeRepository groupChallengeRepository;
    private final GroupChallengeParticipantRecordRepository recordRepository;
    private final GroupChallengeVerificationRepository verificationRepository;
    private final AiGroupChallengeVerificationClient aiClient;

    @Transactional
    public void submit(Long memberId, Long challengeId, GroupChallengeVerificationRequestDto dto) {
        // 챌린지 및 참여 기록 조회
        GroupChallenge challenge = groupChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_NOT_FOUND));

        GroupChallengeParticipantRecord record = recordRepository
                .findByGroupChallengeIdAndMemberIdAndDeletedAtIsNull(challengeId, memberId)
                .orElseThrow(() -> new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_RECORD_NOT_FOUND));

        // 오늘 이미 인증했는지 확인
        LocalDateTime now = LocalDateTime.now();
        boolean alreadySubmitted = verificationRepository
                .findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdOrderByCreatedAtDesc(
                        memberId, challengeId)
                .filter(v -> v.getCreatedAt().toLocalDate().equals(now.toLocalDate()))
                .isPresent();

        if (alreadySubmitted) {
            throw new CustomException(VerificationErrorCode.ALREADY_SUBMITTED);
        }

        // 인증 생성 및 저장
        GroupChallengeVerification verification = GroupChallengeVerification.builder()
                .participantRecord(record)
                .imageUrl(dto.imageUrl())
                .content(dto.content())
                .status(ChallengeStatus.PENDING_APPROVAL)
                .build();

        verificationRepository.save(verification);

        // AI 서버 인증 요청
        AiGroupChallengeVerificationRequestDto aiRequest = AiGroupChallengeVerificationRequestDto.builder()
                .imageUrl(dto.imageUrl())
                .memberId(memberId)
                .challengeId(challengeId)
                .date(now.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .challengeName(challenge.getTitle())
                .build();

        aiClient.verifyImage(aiRequest);
    }
}
