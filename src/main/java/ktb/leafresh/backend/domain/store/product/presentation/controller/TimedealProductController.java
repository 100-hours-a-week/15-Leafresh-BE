package ktb.leafresh.backend.domain.store.product.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ktb.leafresh.backend.domain.store.product.application.service.TimedealProductReadService;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.TimedealProductListResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Timedeal Product", description = "타임딜 상품 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store/products")
@Validated
public class TimedealProductController {

  private final TimedealProductReadService timedealProductReadService;

  @GetMapping("/timedeals")
  @Operation(summary = "타임딜 상품 목록 조회", description = "현재 진행 중인 타임딜 상품 목록을 조회합니다.")
  public ResponseEntity<ApiResponse<TimedealProductListResponseDto>> getTimedealProducts() {

    TimedealProductListResponseDto response = timedealProductReadService.findTimedealProducts();

    String message = response.timeDeals().isEmpty() ? 
        "진행 중인 타임딜 상품이 없습니다." : "타임딜 상품 목록을 불러왔습니다.";

    return ResponseEntity.ok(ApiResponse.success(message, response));
  }
}
