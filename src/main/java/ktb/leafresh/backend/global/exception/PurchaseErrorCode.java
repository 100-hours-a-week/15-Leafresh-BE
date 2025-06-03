package ktb.leafresh.backend.global.exception;

import org.springframework.http.HttpStatus;

public enum PurchaseErrorCode implements BaseErrorCode {

    DUPLICATE_PURCHASE_REQUEST(HttpStatus.BAD_REQUEST, "중복된 주문 요청입니다.");

    private final HttpStatus status;
    private final String message;

    PurchaseErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() { return status; }

    @Override
    public String getMessage() { return message; }
}
