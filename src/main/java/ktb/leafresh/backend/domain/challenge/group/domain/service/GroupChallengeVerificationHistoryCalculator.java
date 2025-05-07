package ktb.leafresh.backend.domain.challenge.group.domain.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeVerificationHistoryResponseDto;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Component
public class GroupChallengeVerificationHistoryCalculator {

    public GroupChallengeVerificationHistoryResponseDto calculate(
            GroupChallenge challenge,
            GroupChallengeParticipantRecord record,
            List<GroupChallengeVerification> verifications
    ) {
        LocalDateTime joinedAt = record.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();

        List<GroupChallengeVerificationHistoryResponseDto.VerificationDto> verificationDtos = verifications.stream()
                .map(v -> GroupChallengeVerificationHistoryResponseDto.VerificationDto.builder()
                        .day((int) ChronoUnit.DAYS.between(joinedAt.toLocalDate(), v.getCreatedAt().toLocalDate()) + 1)
                        .imageUrl(v.getImageUrl())
                        .status(v.getStatus())
                        .build())
                .sorted(Comparator.comparingInt(GroupChallengeVerificationHistoryResponseDto.VerificationDto::day).reversed())
                .toList();

        long success = verifications.stream().filter(v -> v.getStatus() == ChallengeStatus.SUCCESS).count();
        long failure = verifications.stream().filter(v -> v.getStatus() == ChallengeStatus.FAILURE).count();
        long remaining = ChronoUnit.DAYS.between(
                verifications.stream().map(GroupChallengeVerification::getCreatedAt).max(LocalDateTime::compareTo).orElse(joinedAt),
                challenge.getEndDate()
        );

        String todayStatus = verifications.stream()
                .filter(v -> v.getCreatedAt().toLocalDate().isEqual(now.toLocalDate()))
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
