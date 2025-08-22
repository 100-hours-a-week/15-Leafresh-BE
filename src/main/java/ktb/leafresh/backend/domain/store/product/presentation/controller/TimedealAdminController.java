package ktb.leafresh.backend.domain.store.product.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.store.product.application.service.TimedealCreateService;
import ktb.leafresh.backend.domain.store.product.application.service.TimedealUpdateService;
import ktb.leafresh.backend.domain.store.product.presentation.dto.request.TimedealCreateRequestDto;
import ktb.leafresh.backend.domain.store.product.presentation.dto.request.TimedealUpdateRequestDto;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.TimedealCreateResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Timedeal Admin", description = "타임딜 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/timedeals")
@Validated
public class TimedealAdminController {

  private final TimedealCreateService timedealCreateService;
  private final TimedealUpdateService timedealUpdateService;

  @PostMapping
  @Operation(summary = "타임딜 상품 생성", description = "새로운 타임딜 상품을 생성합니다. (관리자 권한 필요)")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<TimedealCreateResponseDto>> createTimedeal(
      @Valid @RequestBody TimedealCreateRequestDto dto) {

    TimedealCreateResponseDto response = timedealCreateService.create(dto);
    return ResponseEntity.ok(ApiResponse.success("타임딜 상품이 등록되었습니다.", response));
  }

  @PatchMapping("/{dealId}")
  @Operation(summary = "타임딜 상품 수정", description = "기존 타임딜 상품 정보를 수정합니다. (관리자 권한 필요)")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> updateTimedeal(
      @Parameter(description = "타임딜 ID") @PathVariable Long dealId,
      @Valid @RequestBody TimedealUpdateRequestDto dto) {

    timedealUpdateService.update(dealId, dto);
    return ResponseEntity.noContent().build();
  }
}
