package ktb.leafresh.backend.global.exception;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {
  HttpStatus getStatus();

  String getMessage();
}
