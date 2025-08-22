package ktb.leafresh.backend.domain.store.order.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import ktb.leafresh.backend.domain.store.order.application.service.ProductPurchaseReadService;
import ktb.leafresh.backend.domain.store.order.presentation.dto.response.ProductPurchaseListResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Product Purchase", description = "상품 구매 내역 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/products")
@Validated
public class ProductPurchaseController {

  private final ProductPurchaseReadService productPurchaseReadService;

  @GetMapping("/list")
  @Operation(summary = "구매 내역 조회", description = "사용자의 상품 구매 내역을 커서 기반 페이지네이션으로 조회합니다.")
  public ResponseEntity<ApiResponse<ProductPurchaseListResponseDto>> getPurchaseHistory(
      @CurrentMemberId Long memberId,
      @Parameter(description = "검색어") @RequestParam(required = false) String input,
      @Parameter(description = "커서 ID") @RequestParam(required = false) Long cursorId,
      @Parameter(description = "커서 타임스탬프") @RequestParam(required = false) String cursorTimestamp,
      @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "12") @Min(1) @Max(50) int size) {

    ProductPurchaseListResponseDto response =
        productPurchaseReadService.getPurchases(memberId, input, cursorId, cursorTimestamp, size);

    String message = response.getPurchases().isEmpty() ? "구매 내역이 없습니다." : "구매 내역을 불러왔습니다.";

    return ResponseEntity.ok(ApiResponse.success(message, response));
  }
}
