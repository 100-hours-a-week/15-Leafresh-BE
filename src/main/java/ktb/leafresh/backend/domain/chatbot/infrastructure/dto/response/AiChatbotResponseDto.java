package ktb.leafresh.backend.domain.chatbot.infrastructure.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "AI 챗봇 응답 DTO")
public record AiChatbotResponseDto(
    @Schema(description = "AI 챗봇 추천 메시지", example = "환경 보호를 위한 다양한 챌린지를 추천드립니다!") String recommend,
    @Schema(description = "추천 챌린지 목록") List<Challenge> challenges) {

  @Schema(description = "챌린지 정보")
  public record Challenge(
      @Schema(description = "챌린지 제목", example = "텀블러 사용하기") String title,
      @Schema(description = "챌린지 설명", example = "일회용 컵 대신 개인 텀블러를 사용해보세요") String description,
      @Schema(description = "챌린지 카테고리", example = "비건") String category,
      @Schema(description = "챌린지 라벨", example = "VEGAN") String label) {}
}
