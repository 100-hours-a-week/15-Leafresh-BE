package ktb.leafresh.backend.domain.member.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import ktb.leafresh.backend.domain.member.application.service.ProfileCardReadService;
import ktb.leafresh.backend.domain.member.presentation.dto.response.ProfileCardResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class ProfileCardController {

    private final ProfileCardReadService profileCardReadService;

    @GetMapping("/profilecard")
    @Operation(summary = "프로필 카드 조회", description = "사용자의 닉네임, 트리 레벨, 누적 포인트, 최근 획득 뱃지 등을 포함한 프로필 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<ProfileCardResponseDto>> getProfileCard(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails.getMemberId();
        log.debug("[프로필 카드 조회] 요청 - memberId: {}", memberId);

        ProfileCardResponseDto response = profileCardReadService.getProfileCard(memberId);

        return ResponseEntity.ok(
                ApiResponse.success("프로필 카드 조회에 성공하였습니다.", response)
        );
    }
}
