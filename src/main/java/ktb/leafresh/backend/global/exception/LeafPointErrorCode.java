package ktb.leafresh.backend.global.exception;

import org.springframework.http.HttpStatus;

public enum LeafPointErrorCode implements BaseErrorCode {
  REDIS_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 조회에 실패했습니다."),
  DB_QUERY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DB에서 누적 나뭇잎 수 조회에 실패했습니다.");

  private final HttpStatus status;
  private final String message;

  LeafPointErrorCode(HttpStatus status, String message) {
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
