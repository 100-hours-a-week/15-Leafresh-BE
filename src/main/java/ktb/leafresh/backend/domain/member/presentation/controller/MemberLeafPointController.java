package ktb.leafresh.backend.domain.member.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ktb.leafresh.backend.domain.member.application.service.MemberLeafPointReadService;
import ktb.leafresh.backend.domain.member.presentation.dto.response.MemberLeafPointResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.response.ApiResponseConstants;
import ktb.leafresh.backend.global.security.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member Leaf Point", description = "회원 나뭇잎 포인트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Validated
public class MemberLeafPointController {

  private final MemberLeafPointReadService memberLeafPointReadService;

  @GetMapping("/leaves")
  @Operation(summary = "나뭇잎 보유량 조회", description = "현재 로그인한 사용자의 보유 나뭇잎 수를 반환합니다.")
  @ApiResponseConstants.ClientErrorResponses
  @ApiResponseConstants.ServerErrorResponses
  public ResponseEntity<ApiResponse<MemberLeafPointResponseDto>> getLeafPoints(
      @CurrentMemberId Long memberId) {

    MemberLeafPointResponseDto responseDto =
        memberLeafPointReadService.getCurrentLeafPoints(memberId);

    return ResponseEntity.ok(ApiResponse.success("보유 나뭇잎 수를 조회했습니다.", responseDto));
  }
}
