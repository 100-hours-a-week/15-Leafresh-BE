package ktb.leafresh.backend.domain.store.product.presentation.controller;

import ktb.leafresh.backend.domain.store.product.application.service.TimedealProductReadService;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.TimedealProductListResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.TimedealErrorCode;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store/products")
public class TimedealProductController {

  private final TimedealProductReadService timedealProductReadService;

  @GetMapping("/timedeals")
  public ResponseEntity<ApiResponse<TimedealProductListResponseDto>> getTimedealProducts() {
    log.info("[타임딜 목록 조회 요청]");

    try {
      TimedealProductListResponseDto response = timedealProductReadService.findTimedealProducts();

      if (response.timeDeals().isEmpty()) {
        return ResponseEntity.ok(ApiResponse.success("진행 중인 타임딜 상품이 없습니다.", response));
      }

      return ResponseEntity.ok(ApiResponse.success("타임딜 상품 목록을 불러왔습니다.", response));

    } catch (Exception e) {
      log.error("[타임딜 목록 조회 실패] message={}", e.getMessage(), e);
      throw new CustomException(TimedealErrorCode.TIMEDEAL_LOAD_FAIL); // 재사용
    }
  }
}
