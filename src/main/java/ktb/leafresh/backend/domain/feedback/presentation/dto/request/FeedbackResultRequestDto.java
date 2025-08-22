package ktb.leafresh.backend.domain.feedback.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FeedbackResultRequestDto(
    @NotNull(message = "memberId는 필수 항목입니다.") Long memberId,
    @NotBlank(message = "feedback는 필수 항목입니다.") String content) {
  /** 응답이 피드백 메시지가 아닌 HTTP status code인 경우를 판단 */
  public boolean isRecoverableHttpError() {
    return content.matches("4\\d\\d|5\\d\\d");
  }

  /** content가 HTTP status code인 경우 정수로 변환 (예외 발생 가능성 있음 → 안전하게 parse 필요 시 사용) */
  public int resultAsHttpStatus() {
    return Integer.parseInt(content);
  }

  /** 정상 피드백 응답인지 여부 */
  public boolean isSuccessResult() {
    return !isRecoverableHttpError();
  }
}
