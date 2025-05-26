package ktb.leafresh.backend.domain.member.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import ktb.leafresh.backend.domain.member.application.service.BadgeReadService;
import ktb.leafresh.backend.domain.member.application.service.RecentBadgeReadService;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.BadgeListResponseDto;
import ktb.leafresh.backend.domain.member.presentation.dto.response.RecentBadgeListResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
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
    private final BadgeReadService badgeReadService;

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

    @GetMapping
    @Operation(summary = "뱃지 목록 전체 조회", description = "회원이 획득한 뱃지와 아직 획득하지 못한 뱃지를 모두 조회합니다.")
    public ResponseEntity<ApiResponse<BadgeListResponseDto>> getAllBadges(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        log.debug("[뱃지 전체 조회 API] 요청 수신 - memberId: {}", memberId);

        try {
            BadgeListResponseDto badgeList = badgeReadService.getAllBadges(memberId);
            return ResponseEntity.ok(ApiResponse.success("뱃지 목록 조회에 성공하였습니다.", badgeList));

        } catch (CustomException e) {
            log.warn("[뱃지 목록 조회] 처리 실패 - memberId: {}, reason: {}", memberId, e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("[뱃지 목록 조회] 서버 내부 오류 발생 - memberId: {}", memberId, e);
            throw new CustomException(MemberErrorCode.BADGE_QUERY_FAILED);
        }
    }
}
