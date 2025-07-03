package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.domain.entity.PersonalChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
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
public class PersonalChallengeVerificationResultQueryService {

    private final PersonalChallengeVerificationRepository verificationRepository;

    @Transactional(readOnly = true)
    public ChallengeStatus getLatestStatus(Long memberId, Long challengeId) {
        ZoneId kst = ZoneId.of("Asia/Seoul");

        LocalDate today = LocalDate.now(kst);
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(23, 59, 59);

        LocalDateTime startUtc = start.atZone(kst).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime endUtc = end.atZone(kst).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        return verificationRepository
                .findTopByMemberIdAndPersonalChallengeIdAndCreatedAtBetween(
                        memberId, challengeId, startUtc, endUtc)
                .map(PersonalChallengeVerification::getStatus)
                .orElse(ChallengeStatus.NOT_SUBMITTED);
    }
}
