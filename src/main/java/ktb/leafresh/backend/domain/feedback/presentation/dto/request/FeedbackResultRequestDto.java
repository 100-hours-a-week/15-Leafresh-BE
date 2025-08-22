package ktb.leafresh.backend.domain.feedback.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(name = "FeedbackResultRequest", description = "외부 AI 서비스로부터 받은 피드백 결과 DTO (내부 시스템 전용)")
public record FeedbackResultRequestDto(
    @Schema(
            description = "피드백을 요청한 회원의 ID",
            example = "12345",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "회원 ID는 필수 항목입니다.")
        @Positive(message = "회원 ID는 양수여야 합니다.")
        Long memberId,
    @Schema(
            description = "AI가 생성한 피드백 내용 또는 HTTP 상태 코드 (오류 시)",
            example =
                "지난주 개인 챌린지를 3개 모두 성공하셨네요! 꾸준한 노력이 돋보입니다. 이번 주에는 그룹 챌린지에도 적극적으로 참여해보시는 것을 추천드려요.",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "피드백 내용은 필수 항목입니다.")
        String content) {

  @Schema(hidden = true)
  public boolean isRecoverableHttpError() {
    return content.matches("4\\d\\d|5\\d\\d");
  }

  @Schema(hidden = true)
  public int resultAsHttpStatus() {
    return Integer.parseInt(content);
  }

  @Schema(hidden = true)
  public boolean isSuccessResult() {
    return !isRecoverableHttpError();
  }
}
