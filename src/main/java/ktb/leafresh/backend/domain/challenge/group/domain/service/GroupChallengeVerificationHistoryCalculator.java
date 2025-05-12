package ktb.leafresh.backend.domain.challenge.group.domain.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeVerificationHistoryResponseDto;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GroupChallengeVerificationHistoryCalculator {

    public GroupChallengeVerificationHistoryResponseDto calculate(
            GroupChallenge challenge,
            GroupChallengeParticipantRecord record,
            List<GroupChallengeVerification> verifications
    ) {
        LocalDate startDate = challenge.getStartDate().toLocalDate();
        LocalDate today = LocalDate.now();

        // 최신 인증이 먼저 보이도록 정렬 + 시작일 기준 day 계산
        List<GroupChallengeVerificationHistoryResponseDto.VerificationDto> verificationDtos = verifications.stream()
                .sorted(Comparator.comparing(GroupChallengeVerification::getCreatedAt).reversed()) // 최신순
                .map(v -> {
                    int day = (int) ChronoUnit.DAYS.between(startDate, v.getCreatedAt().toLocalDate()) + 1;
                    return GroupChallengeVerificationHistoryResponseDto.VerificationDto.builder()
                            .day(day)
                            .imageUrl(v.getImageUrl())
                            .status(v.getStatus())
                            .build();
                })
                .collect(Collectors.toList());

        long success = verifications.stream().filter(v -> v.getStatus() == ChallengeStatus.SUCCESS).count();
        long failure = verifications.stream().filter(v -> v.getStatus() == ChallengeStatus.FAILURE).count();

        LocalDate lastVerificationDate = verifications.stream()
                .map(v -> v.getCreatedAt().toLocalDate())
                .max(LocalDate::compareTo)
                .orElse(startDate);

        long remaining = ChronoUnit.DAYS.between(lastVerificationDate, challenge.getEndDate().toLocalDate());

        String todayStatus = verifications.stream()
                .filter(v -> v.getCreatedAt().toLocalDate().isEqual(today))
                .findFirst()
                .map(v -> switch (v.getStatus()) {
                    case SUCCESS, FAILURE -> "DONE";
                    case PENDING_APPROVAL -> "PENDING_APPROVAL";
                    default -> "NOT_SUBMITTED";
                })
                .orElse("NOT_SUBMITTED");

        return GroupChallengeVerificationHistoryResponseDto.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .achievement(new GroupChallengeVerificationHistoryResponseDto.AchievementDto(
                        (int) success, (int) failure, (int) remaining))
                .verifications(verificationDtos)
                .todayStatus(todayStatus)
                .build();
    }
}
