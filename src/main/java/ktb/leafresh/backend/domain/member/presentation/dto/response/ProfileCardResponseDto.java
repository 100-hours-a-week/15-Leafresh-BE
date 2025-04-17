package ktb.leafresh.backend.domain.member.presentation.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ProfileCardResponseDto(
        String nickname,
        String profileImageUrl,
        Long treeLevelId,
        String treeLevelName,
        String treeImageUrl,
        String nextTreeLevelName,
        String nextTreeImageUrl,
        int totalLeafPoints,
        int leafPointsToNextLevel,
        int totalSuccessfulVerifications,
        int completedGroupChallengesCount,
        List<RecentBadgeDto> badges
) {
    @Builder
    public record RecentBadgeDto(Long id, String name, String imageUrl) {}
}
