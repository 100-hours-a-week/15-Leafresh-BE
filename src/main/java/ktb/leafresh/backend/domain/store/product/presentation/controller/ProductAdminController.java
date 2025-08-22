package ktb.leafresh.backend.domain.store.product.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.store.product.application.service.ProductCreateService;
import ktb.leafresh.backend.domain.store.product.application.service.ProductUpdateService;
import ktb.leafresh.backend.domain.store.product.presentation.dto.request.ProductCreateRequestDto;
import ktb.leafresh.backend.domain.store.product.presentation.dto.request.ProductUpdateRequestDto;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.ProductCreateResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Product Admin", description = "상품 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/products")
@Validated
public class ProductAdminController {

  private final ProductCreateService productCreateService;
  private final ProductUpdateService productUpdateService;

  @PostMapping
  @Operation(summary = "일반 상품 생성", description = "새로운 일반 상품을 생성합니다. (관리자 권한 필요)")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<ProductCreateResponseDto>> createProduct(
      @Valid @RequestBody ProductCreateRequestDto dto) {

    ProductCreateResponseDto response = productCreateService.createProduct(dto);
    return ResponseEntity.ok(ApiResponse.success("일반 상품이 등록되었습니다.", response));
  }

  @PatchMapping("/{productId}")
  @Operation(summary = "일반 상품 수정", description = "기존 일반 상품 정보를 수정합니다. (관리자 권한 필요)")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> updateProduct(
      @Parameter(description = "상품 ID") @PathVariable Long productId,
      @Valid @RequestBody ProductUpdateRequestDto dto) {

    productUpdateService.update(productId, dto);
    return ResponseEntity.noContent().build();
  }
}
