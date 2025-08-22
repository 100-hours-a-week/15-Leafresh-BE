package ktb.leafresh.backend.domain.store.product.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import ktb.leafresh.backend.domain.store.product.application.service.ProductSearchReadService;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.ProductListResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Product", description = "상품 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store/products")
@Validated
public class ProductController {

  private final ProductSearchReadService productSearchReadService;

  @GetMapping
  @Operation(summary = "상품 목록 조회", description = "검색어와 커서 기반 페이지네이션으로 상품 목록을 조회합니다.")
  public ResponseEntity<ApiResponse<ProductListResponseDto>> getProducts(
      @Parameter(description = "검색어") @RequestParam(required = false) String input,
      @Parameter(description = "커서 ID") @RequestParam(required = false) Long cursorId,
      @Parameter(description = "커서 타임스탬프") @RequestParam(required = false) String cursorTimestamp,
      @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "12") @Min(1) @Max(50) int size) {

    ProductListResponseDto response =
        productSearchReadService.search(input, cursorId, cursorTimestamp, size);
    
    String message = response.getProducts().isEmpty() ? "검색된 상품이 없습니다." : "상품 목록을 조회했습니다.";
    return ResponseEntity.ok(ApiResponse.success(message, response));
  }
}
