package ktb.leafresh.backend.domain.member.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.member.application.service.MemberInfoQueryService;
import ktb.leafresh.backend.domain.member.application.service.MemberUpdateService;
import ktb.leafresh.backend.domain.member.presentation.dto.request.MemberUpdateRequestDto;
import ktb.leafresh.backend.domain.member.presentation.dto.response.MemberInfoResponseDto;
import ktb.leafresh.backend.domain.member.presentation.dto.response.MemberUpdateResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.response.ApiResponseConstants;
import ktb.leafresh.backend.global.security.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "회원 관리 API")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Validated
public class MemberController {

  private final MemberUpdateService memberUpdateService;
  private final MemberInfoQueryService memberInfoQueryService;

  @PatchMapping
  @Operation(summary = "회원 정보 수정", description = "닉네임, 이미지 URL을 수정합니다.")
  @ApiResponseConstants.ClientErrorResponses
  @ApiResponseConstants.ServerErrorResponses
  public ResponseEntity<ApiResponse<MemberUpdateResponseDto>> updateMemberInfo(
      @CurrentMemberId Long memberId,
      @Valid @RequestBody MemberUpdateRequestDto requestDto) {

    MemberUpdateResponseDto responseDto =
        memberUpdateService.updateMemberInfo(memberId, requestDto.getNickname(), requestDto.getImageUrl());

    return ResponseEntity.ok(ApiResponse.success("회원 정보 수정이 완료되었습니다.", responseDto));
  }

  @GetMapping
  @Operation(summary = "회원 정보 조회", description = "로그인한 회원의 정보를 반환합니다.")
  @ApiResponseConstants.ClientErrorResponses
  @ApiResponseConstants.ServerErrorResponses
  public ResponseEntity<ApiResponse<MemberInfoResponseDto>> getMemberInfo(
      @CurrentMemberId Long memberId) {

    MemberInfoResponseDto responseDto = memberInfoQueryService.getMemberInfo(memberId);

    return ResponseEntity.ok(ApiResponse.success("회원 정보 조회에 성공했습니다.", responseDto));
  }
}
