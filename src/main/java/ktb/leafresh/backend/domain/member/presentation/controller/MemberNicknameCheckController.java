package ktb.leafresh.backend.domain.member.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ktb.leafresh.backend.domain.member.application.service.MemberNicknameCheckService;
import ktb.leafresh.backend.domain.member.presentation.dto.response.NicknameCheckResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.response.ApiResponseConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원 닉네임 중복 검사", description = "닉네임 중복 여부를 확인하는 API")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberNicknameCheckController {

    private final MemberNicknameCheckService memberNicknameCheckService;

    @Operation(
            summary = "닉네임 중복 검사",
            description = "닉네임이 이미 사용 중인지 확인합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "중복 여부 반환"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "닉네임이 없거나 형식 오류"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @ApiResponseConstants.ClientErrorResponses
    @ApiResponseConstants.ServerErrorResponses
    @GetMapping("/nickname")
    public ResponseEntity<ApiResponse<NicknameCheckResponseDto>> checkNickname(
            @RequestParam(value = "input", required = false) String input) {

        validateNickname(input);

        try {
            boolean isDuplicated = memberNicknameCheckService.isDuplicated(input);
            String message = isDuplicated ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다.";

            return ResponseEntity.ok(ApiResponse.success(message, new NicknameCheckResponseDto(isDuplicated)));
        } catch (Exception e) {
            throw new CustomException(MemberErrorCode.NICKNAME_CHECK_FAILED);
        }
    }

    private void validateNickname(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new CustomException(MemberErrorCode.NICKNAME_REQUIRED);
        }

        if (!input.matches("^[a-zA-Z0-9가-힣]{1,20}$")) {
            throw new CustomException(MemberErrorCode.NICKNAME_INVALID_FORMAT);
        }
    }
}
