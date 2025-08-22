package ktb.leafresh.backend.domain.store.product.presentation.controller;

import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.store.product.application.service.TimedealCreateService;
import ktb.leafresh.backend.domain.store.product.application.service.TimedealUpdateService;
import ktb.leafresh.backend.domain.store.product.presentation.dto.request.TimedealCreateRequestDto;
import ktb.leafresh.backend.domain.store.product.presentation.dto.request.TimedealUpdateRequestDto;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.TimedealCreateResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/timedeals")
@Slf4j
public class TimedealAdminController {

  private final TimedealCreateService timedealCreateService;
  private final TimedealUpdateService timedealUpdateService;

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<ApiResponse<TimedealCreateResponseDto>> createTimedeal(
      @Valid @RequestBody TimedealCreateRequestDto dto) {
    TimedealCreateResponseDto response = timedealCreateService.create(dto);
    return ResponseEntity.ok(ApiResponse.success("타임딜 상품이 등록되었습니다.", response));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{dealId}")
  public ResponseEntity<Void> updateTimedeal(
      @PathVariable Long dealId, @Valid @RequestBody TimedealUpdateRequestDto dto) {
    timedealUpdateService.update(dealId, dto);
    return ResponseEntity.noContent().build();
  }
}
