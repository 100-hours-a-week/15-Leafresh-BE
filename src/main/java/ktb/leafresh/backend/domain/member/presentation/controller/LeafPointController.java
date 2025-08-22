package ktb.leafresh.backend.domain.member.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ktb.leafresh.backend.domain.member.application.service.LeafPointReadService;
import ktb.leafresh.backend.domain.member.presentation.dto.response.TotalLeafPointResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Leaf Point", description = "나뭇잎 포인트 API")
@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeafPointController {

  private final LeafPointReadService leafPointReadService;

  @GetMapping("/count")
  @Operation(summary = "전체 나뭇잎 수 조회", description = "플랫폼 전체의 누적 나뭇잎 수를 조회합니다.")
  public ApiResponse<TotalLeafPointResponseDto> getTotalLeafPoints() {
    TotalLeafPointResponseDto result = leafPointReadService.getTotalLeafPoints();
    return ApiResponse.success("누적 나뭇잎 수 조회에 성공했습니다.", result);
  }
}
