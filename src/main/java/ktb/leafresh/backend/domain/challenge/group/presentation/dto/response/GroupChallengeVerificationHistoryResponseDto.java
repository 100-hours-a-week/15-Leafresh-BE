package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record GroupChallengeVerificationHistoryResponseDto(
        Long id,
        String title,
        AchievementDto achievement,
        List<VerificationDto> verifications,
        String todayStatus
) {
    @Builder
    public record AchievementDto(int success, int failure, int remaining) {}

    @Builder
    public record VerificationDto(int day, String imageUrl, ChallengeStatus status) {}
}
