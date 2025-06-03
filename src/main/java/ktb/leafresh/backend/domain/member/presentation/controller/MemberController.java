package ktb.leafresh.backend.domain.member.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ktb.leafresh.backend.domain.member.application.service.MemberInfoQueryService;
import ktb.leafresh.backend.domain.member.application.service.MemberUpdateService;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.request.MemberUpdateRequestDto;
import ktb.leafresh.backend.domain.member.presentation.dto.response.MemberInfoResponseDto;
import ktb.leafresh.backend.domain.member.presentation.dto.response.MemberUpdateResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.response.ApiResponseConstants;
import ktb.leafresh.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Validated
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberUpdateService memberUpdateService;
    private final MemberInfoQueryService memberInfoQueryService;

    @PatchMapping
    @Operation(summary = "회원 정보 수정", description = "닉네임, 이미지 URL을 수정합니다.")
    @ApiResponseConstants.ClientErrorResponses
    @ApiResponseConstants.ServerErrorResponses
    public ResponseEntity<ApiResponse<MemberUpdateResponseDto>> updateMemberInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MemberUpdateRequestDto requestDto) {

        log.debug("[회원 정보 수정] API 호출 시작 - memberId: {}", userDetails.getMemberId());

        try {
            Member member = memberRepository.findById(userDetails.getMemberId())
                    .orElseThrow(() -> {
                        log.warn("[회원 정보 수정] 존재하지 않는 회원 - memberId: {}", userDetails.getMemberId());
                        return new CustomException(MemberErrorCode.MEMBER_NOT_FOUND);
                    });

            MemberUpdateResponseDto responseDto = memberUpdateService.updateMemberInfo(
                    member, requestDto.getNickname(), requestDto.getImageUrl()
            );

            log.info("[회원 정보 수정] 성공 - memberId: {}", userDetails.getMemberId());

            return ResponseEntity.ok(ApiResponse.success("회원 정보 수정이 완료되었습니다.", responseDto));

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[회원 정보 수정] 처리 중 서버 내부 오류 발생", e);
            throw new CustomException(MemberErrorCode.NICKNAME_UPDATE_FAILED);
        }
    }

    @GetMapping
    @Operation(summary = "회원 정보 조회", description = "로그인한 회원의 정보를 반환합니다.")
    @ApiResponseConstants.ClientErrorResponses
    @ApiResponseConstants.ServerErrorResponses
    public ResponseEntity<ApiResponse<MemberInfoResponseDto>> getMemberInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.debug("[회원 정보 조회] API 호출 시작 - memberId: {}", userDetails.getMemberId());

        try {
            MemberInfoResponseDto responseDto = memberInfoQueryService.getMemberInfo(userDetails.getMemberId());

            log.debug("[회원 정보 조회] API 응답 완료");
            return ResponseEntity.ok(ApiResponse.success("회원 정보 조회에 성공했습니다.", responseDto));

        } catch (Exception e) {
            log.error("[회원 정보 조회] 처리 중 서버 내부 오류 발생", e);
            throw new CustomException(MemberErrorCode.MEMBER_INFO_QUERY_FAILED);
        }
    }
}
