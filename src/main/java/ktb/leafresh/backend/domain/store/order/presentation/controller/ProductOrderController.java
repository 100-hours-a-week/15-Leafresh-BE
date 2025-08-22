package ktb.leafresh.backend.domain.store.order.presentation.controller;

import ktb.leafresh.backend.domain.store.order.application.service.ProductOrderCreateService;
import ktb.leafresh.backend.domain.store.order.application.service.TimedealOrderCreateService;
import ktb.leafresh.backend.domain.store.order.presentation.dto.request.ProductOrderCreateRequestDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@Slf4j
public class ProductOrderController {

  private final ProductOrderCreateService productOrderCreateService;
  private final TimedealOrderCreateService timedealOrderCreateService;

  @PostMapping("/products/{productId}")
  public ResponseEntity<ApiResponse<Void>> createOrder(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long productId,
      @RequestBody ProductOrderCreateRequestDto request,
      @RequestHeader("Idempotency-Key") String idempotencyKey) {
    if (userDetails == null) {
      throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
    }

    Long memberId = userDetails.getMemberId();
    log.info(
        "[일반 상품 주문 요청] memberId={}, productId={}, quantity={}, key={}",
        memberId,
        productId,
        request.quantity(),
        idempotencyKey);

    productOrderCreateService.create(memberId, productId, request.quantity(), idempotencyKey);

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/timedeals/{dealId}")
  public ResponseEntity<ApiResponse<Void>> createTimedealOrder(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long dealId,
      @RequestBody ProductOrderCreateRequestDto request,
      @RequestHeader("Idempotency-Key") String idempotencyKey) {
    if (userDetails == null) {
      throw new CustomException(GlobalErrorCode.UNAUTHORIZED);
    }

    Long memberId = userDetails.getMemberId();
    log.info(
        "[타임딜 상품 주문 요청] memberId={}, dealId={}, quantity={}, key={}",
        memberId,
        dealId,
        request.quantity(),
        idempotencyKey);

    timedealOrderCreateService.create(
        userDetails.getMemberId(), dealId, request.quantity(), idempotencyKey);
    return ResponseEntity.noContent().build();
  }
}
