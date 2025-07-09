package ktb.leafresh.backend.global.exception;

import org.springframework.http.HttpStatus;

public enum TimedealErrorCode implements BaseErrorCode {
    PRODUCT_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 상품을 찾을 수 없습니다."),
    INVALID_TIME(HttpStatus.BAD_REQUEST, "시작 시간은 종료 시간보다 앞서야 합니다."),
    OVERLAPPING_TIME(HttpStatus.BAD_REQUEST, "해당 시간대에 이미 타임딜이 등록되어 있습니다."),
    TIMEDEAL_SAVE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "타임딜 정책을 연결하지 못했습니다."),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "할인 가격은 1 이상이어야 합니다."),
    INVALID_PERCENT(HttpStatus.BAD_REQUEST, "할인율은 1 이상이어야 합니다."),
    INVALID_STOCK(HttpStatus.BAD_REQUEST, "재고는 0 이상이어야 합니다."),
    TIMEDEAL_LOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "타임딜 상품을 불러오지 못했습니다. 잠시 후 다시 시도해주세요."),
    TIMEDEAL_POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 타임딜 정책을 찾을 수 없습니다."),
    INVALID_PRODUCT_FOR_TIMEDEAL(HttpStatus.BAD_REQUEST, "해당 상품은 선택한 타임딜 정책에 속하지 않습니다.");

    private final HttpStatus status;
    private final String message;

    TimedealErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}
