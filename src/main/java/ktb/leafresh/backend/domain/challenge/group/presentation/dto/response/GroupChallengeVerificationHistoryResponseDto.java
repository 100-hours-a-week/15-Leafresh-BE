package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import lombok.Builder;

import java.util.List;

@Schema(description = "단체 챌린지 인증 히스토리 응답")
@Builder
public record GroupChallengeVerificationHistoryResponseDto(
    @Schema(description = "챌린지 ID", example = "1") Long id,
    @Schema(description = "챌린지 제목", example = "제로웨이스트 챌린지") String title,
    @Schema(description = "달성 현황") AchievementDto achievement,
    @Schema(description = "인증 내역 목록") List<VerificationDto> verifications,
    @Schema(description = "오늘 인증 상태", example = "SUCCESS") String todayStatus) {

  @Schema(description = "달성 현황 정보")
  @Builder
  public record AchievementDto(
      @Schema(description = "성공 횟수", example = "15") int success,
      @Schema(description = "실패 횟수", example = "3") int failure,
      @Schema(description = "남은 횟수", example = "12") int remaining) {}

  @Schema(description = "인증 내역 정보")
  @Builder
  public record VerificationDto(
      @Schema(description = "일차", example = "1") int day,
      @Schema(description = "인증 이미지 URL", example = "https://leafresh.io/verification.jpg")
          String imageUrl,
      @Schema(description = "인증 상태", example = "SUCCESS") ChallengeStatus status) {}
}
