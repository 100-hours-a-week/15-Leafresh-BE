package ktb.leafresh.backend.domain.store.order.presentation.controller;

import ktb.leafresh.backend.domain.store.order.application.service.ProductPurchaseReadService;
import ktb.leafresh.backend.domain.store.order.presentation.dto.response.ProductPurchaseListResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/products")
@Slf4j
public class ProductPurchaseController {

    private final ProductPurchaseReadService productPurchaseReadService;

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<ProductPurchaseListResponseDto>> getPurchaseHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String input,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(required = false) String cursorTimestamp,
            @RequestParam(defaultValue = "12") int size
    ) {
        if ((cursorId == null) != (cursorTimestamp == null)) {
            throw new CustomException(GlobalErrorCode.INVALID_CURSOR);
        }

        try {
            ProductPurchaseListResponseDto response = productPurchaseReadService.getPurchases(userDetails.getMemberId(), input, cursorId, cursorTimestamp, size);

            String message = response.getPurchases().isEmpty()
                    ? "구매 내역이 없습니다."
                    : "구매 내역을 불러왔습니다.";

            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception e) {
            log.error("[구매 내역 조회 실패] {}", e.getMessage(), e);
            throw new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
