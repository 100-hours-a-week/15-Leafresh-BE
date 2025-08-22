package ktb.leafresh.backend.global.exception;

import org.springframework.http.HttpStatus;

public enum NotificationErrorCode implements BaseErrorCode {
  NOTIFICATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "알림 조회 권한이 없습니다."),
  NOTIFICATION_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류로 인해 알림 조회에 실패하였습니다."),
  NOTIFICATION_MARK_READ_FAILED(
      HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류로 인해 알림 읽음 처리에 실패하였습니다."),
  NOTIFICATION_MARK_READ_ACCESS_DENIED(HttpStatus.FORBIDDEN, "알림 읽음 처리 권한이 없습니다."),
  INVALID_NOTIFICATION_REQUEST(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.");

  private final HttpStatus status;
  private final String message;

  NotificationErrorCode(HttpStatus status, String message) {
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
