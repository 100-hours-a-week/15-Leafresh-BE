package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Schema(description = "단체 챌린지 인증 상세 정보 응답")
@Builder
public record GroupChallengeVerificationDetailResponseDto(
    @Schema(description = "인증 ID", example = "1") Long id,
    @Schema(description = "인증한 사용자 닉네임", example = "leafresh") String nickname,
    @Schema(description = "사용자 프로필 이미지 URL", example = "https://leafresh.io/profile.jpg")
        String profileImageUrl,
    @Schema(description = "좋아요 여부", example = "true") boolean isLiked,
    @Schema(description = "인증 이미지 URL", example = "https://leafresh.io/verification.jpg")
        String imageUrl,
    @Schema(description = "인증 내용", example = "오늘도 제로웨이스트 실천했어요!") String content,
    @Schema(description = "챌린지 카테고리", example = "ZERO_WASTE") String category,
    @Schema(description = "인증 상태", example = "SUCCESS") String status,
    @Schema(description = "인증 완료 시간", example = "2024-01-01T10:00:00+00:00")
        OffsetDateTime verifiedAt,
    @Schema(description = "통계 정보") Counts counts,
    @Schema(description = "생성일시", example = "2024-01-01T10:00:00+00:00") OffsetDateTime createdAt,
    @Schema(description = "수정일시", example = "2024-01-01T10:00:00+00:00") OffsetDateTime updatedAt) {

  @Schema(description = "인증 통계 정보")
  @Builder
  public record Counts(
      @Schema(description = "조회수", example = "100") int view,
      @Schema(description = "좋아요 수", example = "25") int like,
      @Schema(description = "댓글 수", example = "5") int comment) {}

  public static GroupChallengeVerificationDetailResponseDto from(
      GroupChallengeVerification v, Map<Object, Object> cachedStats, boolean isLiked) {
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
        .counts(
            new Counts(
                parseStat(cachedStats, "viewCount", v.getViewCount()),
                parseStat(cachedStats, "likeCount", v.getLikeCount()),
                parseStat(cachedStats, "commentCount", v.getCommentCount())))
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
