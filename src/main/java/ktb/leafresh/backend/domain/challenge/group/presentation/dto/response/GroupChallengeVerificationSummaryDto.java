package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import lombok.Builder;

@Builder
public record GroupChallengeVerificationSummaryDto(
        Long id,
        String nickname,
        String profileImageUrl,
        String verificationImageUrl,
        String description
) {
    public static GroupChallengeVerificationSummaryDto from(GroupChallengeVerification verification) {
        var member = verification.getParticipantRecord().getMember();
        return GroupChallengeVerificationSummaryDto.builder()
                .id(verification.getId())
                .nickname(member.getNickname())
                .profileImageUrl(member.getImageUrl())
                .verificationImageUrl(verification.getImageUrl())
                .description(verification.getContent())
                .build();
    }
}
