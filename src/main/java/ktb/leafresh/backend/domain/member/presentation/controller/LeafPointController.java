package ktb.leafresh.backend.domain.member.presentation.controller;

import ktb.leafresh.backend.domain.member.application.service.LeafPointReadService;
import ktb.leafresh.backend.domain.member.presentation.dto.response.TotalLeafPointResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeafPointController {

    private final LeafPointReadService leafPointReadService;

    @GetMapping("/count")
    public ApiResponse<TotalLeafPointResponseDto> getTotalLeafPoints() {
        TotalLeafPointResponseDto result = leafPointReadService.getTotalLeafPoints();
        return ApiResponse.success("누적 나뭇잎 수 조회에 성공했습니다.", result);
    }
}
