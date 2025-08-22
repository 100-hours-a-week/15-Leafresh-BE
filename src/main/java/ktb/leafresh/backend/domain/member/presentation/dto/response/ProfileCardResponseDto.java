package ktb.leafresh.backend.domain.member.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "프로필 카드 응답 DTO")
@Builder
public record ProfileCardResponseDto(
    @Schema(description = "닉네임", example = "리프레시유저") String nickname,
    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String profileImageUrl,
    @Schema(description = "현재 나무 레벨 ID", example = "1") Long treeLevelId,
    @Schema(description = "현재 나무 레벨 이름", example = "SEEDLING") String treeLevelName,
    @Schema(description = "현재 나무 이미지 URL", example = "https://example.com/tree.jpg")
        String treeImageUrl,
    @Schema(description = "다음 나무 레벨 이름", example = "YOUNG_TREE") String nextTreeLevelName,
    @Schema(description = "다음 나무 이미지 URL", example = "https://example.com/next_tree.jpg")
        String nextTreeImageUrl,
    @Schema(description = "총 리프 포인트", example = "150") int totalLeafPoints,
    @Schema(description = "다음 레벨까지 필요한 리프 포인트", example = "50") int leafPointsToNextLevel,
    @Schema(description = "총 인증 성공 횟수", example = "25") int totalSuccessfulVerifications,
    @Schema(description = "완료한 그룹 챌린지 개수", example = "5") int completedGroupChallengesCount,
    @Schema(description = "최근 획득한 배지 목록") List<RecentBadgeDto> badges) {

  @Schema(description = "최근 배지 DTO")
  @Builder
  public record RecentBadgeDto(
      @Schema(description = "배지 ID", example = "1") Long id,
      @Schema(description = "배지 이름", example = "첫 걸음") String name,
      @Schema(description = "배지 이미지 URL", example = "https://example.com/badge.jpg")
          String imageUrl) {}
}
