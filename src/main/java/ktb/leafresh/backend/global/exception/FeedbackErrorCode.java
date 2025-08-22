package ktb.leafresh.backend.global.exception;

import org.springframework.http.HttpStatus;

public enum FeedbackErrorCode implements BaseErrorCode {
  NO_CHALLENGE_ACTIVITY(
      HttpStatus.UNPROCESSABLE_ENTITY, "피드백 생성을 위한 활동 데이터가 부족합니다. 최소 1개의 챌린지 참여가 필요합니다."),
  FEEDBACK_SERVER_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류로 피드백 생성을 완료하지 못했습니다. 잠시 후 다시 시도해주세요."),
  ALREADY_FEEDBACK_EXISTS(HttpStatus.CONFLICT, "이미 피드백 결과가 저장되어 있습니다."),
  FEEDBACK_SAVE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류로 피드백 결과 저장에 실패했습니다. 잠시 후 다시 시도해주세요."),
  FEEDBACK_NOT_READY(HttpStatus.NOT_FOUND, "피드백 결과가 아직 준비되지 않았습니다.");

  private final HttpStatus status;
  private final String message;

  FeedbackErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }
}
