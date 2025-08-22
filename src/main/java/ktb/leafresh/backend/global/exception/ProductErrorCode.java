package ktb.leafresh.backend.global.exception;

import org.springframework.http.HttpStatus;

public enum ProductErrorCode implements BaseErrorCode {
  PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 상품을 찾을 수 없습니다."),
  INVALID_PRICE(HttpStatus.BAD_REQUEST, "가격은 1 이상이어야 합니다."),
  INVALID_STOCK(HttpStatus.BAD_REQUEST, "재고는 0 이상이어야 합니다."),
  INVALID_STATUS(HttpStatus.BAD_REQUEST, "유효하지 않은 상품 상태입니다."),
  PRODUCT_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류로 인해 상품을 등록하지 못했습니다."),
  PRODUCT_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류로 인해 상품을 수정하지 못했습니다."),
  OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "재고가 없습니다.");

  private final HttpStatus status;
  private final String message;

  ProductErrorCode(HttpStatus status, String message) {
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
