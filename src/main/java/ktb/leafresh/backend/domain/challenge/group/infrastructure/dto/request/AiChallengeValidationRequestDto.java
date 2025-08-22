package ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "AI 챌린지 검증 요청 DTO")
public record AiChallengeValidationRequestDto(
    @Schema(description = "회원 ID", example = "1") Long memberId,
    @Schema(description = "챌린지 이름", example = "제로웨이스트 챌린지") String challengeName,
    @Schema(description = "시작일", example = "2024-01-01") String startDate,
    @Schema(description = "종료일", example = "2024-01-31") String endDate,
    @Schema(description = "챌린지 요약 목록") List<ChallengeSummary> challenge) {

  @Schema(description = "챌린지 요약 정보")
  public record ChallengeSummary(
      @Schema(description = "챌린지 ID", example = "1") Long id,
      @Schema(description = "챌린지 이름", example = "제로웨이스트 챌린지") String name,
      @Schema(description = "시작일", example = "2024-01-01") String startDate,
      @Schema(description = "종료일", example = "2024-01-31") String endDate) {

    @Override
    public String toString() {
      return String.format(
          "{id=%d, name='%s', startDate=%s, endDate=%s}", id, name, startDate, endDate);
    }
  }
}
