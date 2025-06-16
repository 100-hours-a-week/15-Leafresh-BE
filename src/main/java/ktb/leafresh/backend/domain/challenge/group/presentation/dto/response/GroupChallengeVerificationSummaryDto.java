package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Builder
public record GroupChallengeVerificationSummaryDto(
        Long id,
        String nickname,
        String profileImageUrl,
        String verificationImageUrl,
        String description,
        String category,
        Counts counts,
        OffsetDateTime createdAt,
        Boolean isLiked
) {
    @Builder
    public record Counts(
            int view,
            int like,
            int comment
    ) {}

    public static GroupChallengeVerificationSummaryDto from(
            GroupChallengeVerification verification,
            Map<Object, Object> cachedStats,
            boolean isLiked
    ) {
        var member = verification.getParticipantRecord().getMember();
        var challenge = verification.getParticipantRecord().getGroupChallenge();

        return GroupChallengeVerificationSummaryDto.builder()
                .id(verification.getId())
                .nickname(member.getNickname())
                .profileImageUrl(member.getImageUrl())
                .verificationImageUrl(verification.getImageUrl())
                .description(verification.getContent())
                .category(challenge.getCategory().getName())
                .counts(new Counts(
                        parseCount(cachedStats, "viewCount", verification.getViewCount()),
                        parseCount(cachedStats, "likeCount", verification.getLikeCount()),
                        parseCount(cachedStats, "commentCount", verification.getCommentCount())
                ))
                .createdAt(verification.getCreatedAt().atOffset(ZoneOffset.UTC))
                .isLiked(isLiked)
                .build();
    }

    private static int parseCount(Map<Object, Object> map, String key, int fallback) {
        if (map == null || !map.containsKey(key)) return fallback;
        try {
            return Integer.parseInt(map.get(key).toString());
        } catch (Exception e) {
            return fallback;
        }
    }
}
