package ktb.leafresh.backend.domain.member.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ktb.leafresh.backend.domain.member.application.service.MemberLeafPointReadService;
import ktb.leafresh.backend.domain.member.presentation.dto.response.MemberLeafPointResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.response.ApiResponseConstants;
import ktb.leafresh.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberLeafPointController {

  private final MemberLeafPointReadService memberLeafPointReadService;

  @GetMapping("/leaves")
  @Operation(summary = "나뭇잎 보유량 조회", description = "현재 로그인한 사용자의 보유 나뭇잎 수를 반환합니다.")
  @ApiResponseConstants.ClientErrorResponses
  @ApiResponseConstants.ServerErrorResponses
  public ResponseEntity<ApiResponse<MemberLeafPointResponseDto>> getLeafPoints(
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    Long memberId = userDetails.getMemberId();
    log.debug("[나뭇잎 보유량 조회] 요청 시작 - memberId: {}", memberId);

    MemberLeafPointResponseDto responseDto =
        memberLeafPointReadService.getCurrentLeafPoints(memberId);

    log.debug(
        "[나뭇잎 보유량 조회] 응답 완료 - memberId: {}, currentLeafPoints: {}",
        memberId,
        responseDto.getCurrentLeafPoints());
    return ResponseEntity.ok(ApiResponse.success("보유 나뭇잎 수를 조회했습니다.", responseDto));
  }
}
