package ktb.leafresh.backend.global.exception;

import org.springframework.http.HttpStatus;

public enum VerificationErrorCode implements BaseErrorCode {

    ALREADY_SUBMITTED(HttpStatus.BAD_REQUEST, "오늘은 이미 인증을 완료했습니다."),
    VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 날짜에 인증 내역이 존재하지 않습니다.");

    private final HttpStatus status;
    private final String message;

    VerificationErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}
