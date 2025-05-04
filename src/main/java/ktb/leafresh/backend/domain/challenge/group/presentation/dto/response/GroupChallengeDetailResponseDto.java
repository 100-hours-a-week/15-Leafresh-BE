package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
public record GroupChallengeDetailResponseDto(
        Long id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        LocalTime verificationStartTime,
        LocalTime verificationEndTime,
        Integer leafReward,
        String thumbnailUrl,
        List<GroupChallengeExampleImageDto> exampleImages,
        List<String> verificationImages,
        int maxParticipantCount,
        int currentParticipantCount,
        ChallengeStatus status
) {
    public static GroupChallengeDetailResponseDto of(GroupChallenge challenge,
                                                     List<GroupChallengeExampleImageDto> exampleImages,
                                                     List<String> verificationImages,
                                                     ChallengeStatus status) {
        return GroupChallengeDetailResponseDto.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .startDate(challenge.getStartDate().toLocalDate())
                .endDate(challenge.getEndDate().toLocalDate())
                .verificationStartTime(challenge.getVerificationStartTime())
                .verificationEndTime(challenge.getVerificationEndTime())
                .leafReward(challenge.getLeafReward())
                .thumbnailUrl(challenge.getImageUrl())
                .exampleImages(exampleImages)
                .verificationImages(verificationImages)
                .maxParticipantCount(challenge.getMaxParticipantCount())
                .currentParticipantCount(challenge.getCurrentParticipantCount())
                .status(status)
                .build();
    }
}
