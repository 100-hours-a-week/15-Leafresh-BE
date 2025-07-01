package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class GroupChallengeVerificationResultQueryService {

    private final GroupChallengeVerificationRepository verificationRepository;
//    private final ChallengeVerificationPollingExecutor pollingExecutor;

//    @Transactional(readOnly = true)
//    public ChallengeStatus waitForResult(Long memberId, Long challengeId) {
//        return pollingExecutor.poll(() -> getLatestStatus(memberId, challengeId));
//    }
//
//    private ChallengeStatus getLatestStatus(Long memberId, Long challengeId) {
//        LocalDateTime start = LocalDate.now().atStartOfDay();
//        LocalDateTime end = LocalDate.now().atTime(23, 59, 59);
//
//        return verificationRepository
//                .findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdAndCreatedAtBetween(
//                        memberId, challengeId, start, end)
//                .map(GroupChallengeVerification::getStatus)
//                .orElse(ChallengeStatus.NOT_SUBMITTED);
//    }

    @Transactional(readOnly = true)
    public ChallengeStatus getLatestStatus(Long memberId, Long challengeId) {
        ZoneId kst = ZoneId.of("Asia/Seoul");

        LocalDate today = LocalDate.now(kst);
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(23, 59, 59);

        LocalDateTime startUtc = start.atZone(kst).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime endUtc = end.atZone(kst).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        return verificationRepository
                .findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdAndCreatedAtBetween(
                        memberId, challengeId, startUtc, endUtc)
                .map(GroupChallengeVerification::getStatus)
                .orElse(ChallengeStatus.NOT_SUBMITTED);
    }
}
