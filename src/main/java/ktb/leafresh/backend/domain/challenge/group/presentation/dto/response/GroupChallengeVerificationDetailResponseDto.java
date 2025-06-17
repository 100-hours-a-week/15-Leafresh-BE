package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Builder
public record GroupChallengeVerificationDetailResponseDto(
        Long id,
        String nickname,
        String profileImageUrl,
        boolean isLiked,
        String imageUrl,
        String content,
        String category,
        String status,
        OffsetDateTime verifiedAt,
        Counts counts,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    @Builder
    public record Counts(
            int view,
            int like,
            int comment
    ) {}

    public static GroupChallengeVerificationDetailResponseDto from(
            GroupChallengeVerification v,
            Map<Object, Object> cachedStats,
            boolean isLiked
    ) {
        var member = v.getParticipantRecord().getMember();
        var challenge = v.getParticipantRecord().getGroupChallenge();

        return GroupChallengeVerificationDetailResponseDto.builder()
                .id(v.getId())
                .nickname(member.getNickname())
                .profileImageUrl(member.getImageUrl())
                .isLiked(isLiked)
                .imageUrl(v.getImageUrl())
                .content(v.getContent())
                .category(challenge.getCategory().getName())
                .status(v.getStatus().name())
                .verifiedAt(v.getVerifiedAt() != null ? v.getVerifiedAt().atOffset(ZoneOffset.UTC) : null)
                .counts(new Counts(
                        parseStat(cachedStats, "viewCount", v.getViewCount()),
                        parseStat(cachedStats, "likeCount", v.getLikeCount()),
                        parseStat(cachedStats, "commentCount", v.getCommentCount())
                ))
                .createdAt(v.getCreatedAt().atOffset(ZoneOffset.UTC))
                .updatedAt(v.getUpdatedAt().atOffset(ZoneOffset.UTC))
                .build();
    }

    private static int parseStat(Map<Object, Object> map, String key, int fallback) {
        if (map == null || !map.containsKey(key)) return fallback;
        try {
            return Integer.parseInt(map.get(key).toString());
        } catch (Exception e) {
            return fallback;
        }
    }
}
