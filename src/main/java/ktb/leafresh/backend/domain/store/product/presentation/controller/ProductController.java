package ktb.leafresh.backend.domain.store.product.presentation.controller;

import ktb.leafresh.backend.domain.store.product.application.service.ProductSearchReadService;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.ProductListResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.exception.ProductErrorCode;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store/products")
@Slf4j
public class ProductController {

    private final ProductSearchReadService productSearchReadService;

    @GetMapping
    public ResponseEntity<ApiResponse<ProductListResponseDto>> getProducts(
            @RequestParam(required = false) String input,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) String cursorTimestamp,
            @RequestParam(defaultValue = "12") int size
    ) {
        if ((cursorId == null) != (cursorTimestamp == null)) {
            throw new CustomException(GlobalErrorCode.INVALID_CURSOR);
        }

        try {
            ProductListResponseDto response = productSearchReadService.search(input, cursorId, cursorTimestamp, size);
            String message = response.getProducts().isEmpty()
                    ? "검색된 상품이 없습니다."
                    : "상품 목록을 조회했습니다.";
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception e) {
            log.error("[상품 목록 조회 실패]", e);
            throw new CustomException(ProductErrorCode.PRODUCT_CREATE_FAILED);
        }
    }
}
