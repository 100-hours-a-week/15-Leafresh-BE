package ktb.leafresh.backend.domain.store.product.presentation.controller;

import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.store.product.application.service.ProductCreateService;
import ktb.leafresh.backend.domain.store.product.application.service.ProductUpdateService;
import ktb.leafresh.backend.domain.store.product.presentation.dto.request.ProductCreateRequestDto;
import ktb.leafresh.backend.domain.store.product.presentation.dto.request.ProductUpdateRequestDto;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.ProductCreateResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/products")
@Slf4j
public class ProductAdminController {

  private final ProductCreateService productCreateService;
  private final ProductUpdateService productUpdateService;

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<ApiResponse<ProductCreateResponseDto>> createProduct(
      @Valid @RequestBody ProductCreateRequestDto dto) {
    ProductCreateResponseDto response = productCreateService.createProduct(dto);
    return ResponseEntity.ok(ApiResponse.success("일반 상품이 등록되었습니다.", response));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{productId}")
  public ResponseEntity<Void> updateProduct(
      @PathVariable Long productId, @Valid @RequestBody ProductUpdateRequestDto dto) {
    productUpdateService.update(productId, dto);
    return ResponseEntity.noContent().build();
  }
}
