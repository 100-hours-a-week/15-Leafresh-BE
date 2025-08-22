package ktb.leafresh.backend.domain.store.order.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.store.order.application.service.ProductOrderCreateService;
import ktb.leafresh.backend.domain.store.order.application.service.TimedealOrderCreateService;
import ktb.leafresh.backend.domain.store.order.presentation.dto.request.ProductOrderCreateRequestDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Product Order", description = "상품 주문 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@Validated
public class ProductOrderController {

  private final ProductOrderCreateService productOrderCreateService;
  private final TimedealOrderCreateService timedealOrderCreateService;

  @PostMapping("/products/{productId}")
  @Operation(summary = "일반 상품 주문", description = "일반 상품을 주문합니다.")
  public ResponseEntity<ApiResponse<Void>> createOrder(
      @CurrentMemberId Long memberId,
      @Parameter(description = "상품 ID") @PathVariable Long productId,
      @Valid @RequestBody ProductOrderCreateRequestDto request,
      @Parameter(description = "멱등성 키") @RequestHeader("Idempotency-Key") String idempotencyKey) {

    productOrderCreateService.create(memberId, productId, request.quantity(), idempotencyKey);

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/timedeals/{dealId}")
  @Operation(summary = "타임딜 상품 주문", description = "타임딜 상품을 주문합니다.")
  public ResponseEntity<ApiResponse<Void>> createTimedealOrder(
      @CurrentMemberId Long memberId,
      @Parameter(description = "타임딜 ID") @PathVariable Long dealId,
      @Valid @RequestBody ProductOrderCreateRequestDto request,
      @Parameter(description = "멱등성 키") @RequestHeader("Idempotency-Key") String idempotencyKey) {

    timedealOrderCreateService.create(memberId, dealId, request.quantity(), idempotencyKey);
    return ResponseEntity.noContent().build();
  }
}
