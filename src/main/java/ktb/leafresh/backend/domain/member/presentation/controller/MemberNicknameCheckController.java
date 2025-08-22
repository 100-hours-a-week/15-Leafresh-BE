package ktb.leafresh.backend.domain.member.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import ktb.leafresh.backend.domain.member.application.service.MemberNicknameCheckService;
import ktb.leafresh.backend.domain.member.presentation.dto.response.NicknameCheckResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.response.ApiResponseConstants;
import ktb.leafresh.backend.global.validator.NicknameValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member Nickname Check", description = "회원 닉네임 중복 검사 API")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Validated
public class MemberNicknameCheckController {

  private final MemberNicknameCheckService memberNicknameCheckService;

  @GetMapping("/nickname")
  @Operation(summary = "닉네임 중복 검사", description = "닉네임이 이미 사용 중인지 확인합니다.")
  @ApiResponseConstants.ClientErrorResponses
  @ApiResponseConstants.ServerErrorResponses
  public ResponseEntity<ApiResponse<NicknameCheckResponseDto>> checkNickname(
      @Parameter(description = "검사할 닉네임", required = true) 
      @RequestParam("input") @NotBlank String input) {
    
    NicknameValidator.validate(input);

    boolean isDuplicated = memberNicknameCheckService.isDuplicated(input);
    String message = isDuplicated ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다.";

    return ResponseEntity.ok(
        ApiResponse.success(message, new NicknameCheckResponseDto(isDuplicated)));
  }
}
