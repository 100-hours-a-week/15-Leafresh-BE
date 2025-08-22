package ktb.leafresh.backend.domain.member.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import ktb.leafresh.backend.domain.member.application.service.BadgeReadService;
import ktb.leafresh.backend.domain.member.application.service.RecentBadgeReadService;
import ktb.leafresh.backend.domain.member.presentation.dto.response.BadgeListResponseDto;
import ktb.leafresh.backend.domain.member.presentation.dto.response.RecentBadgeListResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member Badge", description = "회원 뱃지 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/badges")
@Validated
public class MemberBadgeController {

  private final RecentBadgeReadService recentBadgeReadService;
  private final BadgeReadService badgeReadService;

  @GetMapping("/recent")
  @Operation(summary = "최근 획득한 뱃지 조회", description = "회원이 최근에 획득한 뱃지를 최신순으로 조회합니다.")
  public ResponseEntity<ApiResponse<RecentBadgeListResponseDto>> getRecentBadges(
      @CurrentMemberId Long memberId,
      @Parameter(description = "조회할 뱃지 개수") @RequestParam(defaultValue = "8") @Min(1) @Max(50)
          int count) {

    RecentBadgeListResponseDto badges = recentBadgeReadService.getRecentBadges(memberId, count);

    return ResponseEntity.ok(ApiResponse.success("최근 획득한 뱃지 조회에 성공하였습니다.", badges));
  }

  @GetMapping
  @Operation(summary = "뱃지 목록 전체 조회", description = "회원이 획득한 뱃지와 아직 획득하지 못한 뱃지를 모두 조회합니다.")
  public ResponseEntity<ApiResponse<BadgeListResponseDto>> getAllBadges(
      @CurrentMemberId Long memberId) {

    BadgeListResponseDto badgeList = badgeReadService.getAllBadges(memberId);
    return ResponseEntity.ok(ApiResponse.success("뱃지 목록 조회에 성공하였습니다.", badgeList));
  }
}
