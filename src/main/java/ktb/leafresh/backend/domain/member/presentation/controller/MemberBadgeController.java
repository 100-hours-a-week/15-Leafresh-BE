package ktb.leafresh.backend.domain.member.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import ktb.leafresh.backend.domain.member.application.service.RecentBadgeReadService;
import ktb.leafresh.backend.domain.member.presentation.dto.response.RecentBadgeListResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/badges")
public class MemberBadgeController {

    private final RecentBadgeReadService recentBadgeReadService;

    @GetMapping("/recent")
    @Operation(summary = "최근 획득한 뱃지 조회", description = "회원이 최근에 획득한 뱃지를 최신순으로 조회합니다.")
    public ResponseEntity<ApiResponse<RecentBadgeListResponseDto>> getRecentBadges(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "8") int count
    ) {
        Long memberId = userDetails.getMemberId();
        log.debug("[최근 뱃지 조회] API 요청 시작 - memberId: {}, count: {}", memberId, count);

        RecentBadgeListResponseDto badges = recentBadgeReadService.getRecentBadges(memberId, count);

        return ResponseEntity.ok(ApiResponse.success("최근 획득한 뱃지 조회에 성공하였습니다.", badges));
    }
}
