package ktb.leafresh.backend.global.exception;

import org.springframework.http.HttpStatus;

public enum PurchaseErrorCode implements BaseErrorCode {

    DUPLICATE_PURCHASE_REQUEST(HttpStatus.CONFLICT, "중복된 주문 요청입니다."),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "재고가 부족합니다."),
    INSUFFICIENT_POINTS(HttpStatus.BAD_REQUEST, "보유한 나뭇잎 포인트가 부족합니다."),
    PURCHASE_SERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "구매 요청 직렬화에 실패했습니다."),
    PURCHASE_PUBLISH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "구매 요청 발행에 실패했습니다.");

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
