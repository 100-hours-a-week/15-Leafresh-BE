package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GroupChallengeVerificationSummaryDto(
        Long id,
        String nickname,
        String profileImageUrl,
        String verificationImageUrl,
        String description,
        LocalDateTime createdAt
) {
    public static GroupChallengeVerificationSummaryDto from(GroupChallengeVerification verification) {
        var member = verification.getParticipantRecord().getMember();
        return GroupChallengeVerificationSummaryDto.builder()
                .id(verification.getId())
                .nickname(member.getNickname())
                .profileImageUrl(member.getImageUrl())
                .verificationImageUrl(verification.getImageUrl())
                .description(verification.getContent())
                .createdAt(verification.getCreatedAt())
                .build();
    }

    public LocalDateTime createdAt() {
        return this.createdAt;
    }
}
