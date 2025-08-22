package ktb.leafresh.backend.domain.feedback.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(name = "FeedbackResponse", description = "피드백 조회 응답 DTO")
@Getter
@AllArgsConstructor
public class FeedbackResponseDto {

  @Schema(
      description = "AI가 생성한 개인 맞춤 피드백 내용",
      example =
          "지난주 개인 챌린지를 3개 모두 성공하셨네요! 꾸준한 노력이 돋보입니다. 이번 주에는 그룹 챌린지에도 적극적으로 참여해보시는 것을 추천드려요. 환경 보호 활동을 통해 더 많은 사람들과 연결되는 경험을 해보세요.",
      nullable = true)
  private final String content; // null 가능 - 아직 피드백이 생성되지 않은 경우
}
