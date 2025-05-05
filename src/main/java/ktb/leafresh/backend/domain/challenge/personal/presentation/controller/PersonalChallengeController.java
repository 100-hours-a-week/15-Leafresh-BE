package ktb.leafresh.backend.domain.challenge.personal.presentation.controller;

import ktb.leafresh.backend.domain.challenge.personal.application.service.PersonalChallengeReadService;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response.PersonalChallengeDetailResponseDto;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response.PersonalChallengeListResponseDto;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/personal")
public class PersonalChallengeController {

    private final PersonalChallengeReadService readService;

    @GetMapping
    public ResponseEntity<ApiResponse<PersonalChallengeListResponseDto>> getPersonalChallengesByDay(
            @RequestParam DayOfWeek dayOfWeek
    ) {
        PersonalChallengeListResponseDto response = readService.getByDayOfWeek(dayOfWeek);
        return ResponseEntity.ok(ApiResponse.success("개인챌린지 목록 조회에 성공하였습니다.", response));
    }

    @GetMapping("/{challengeId}")
    public ResponseEntity<ApiResponse<PersonalChallengeDetailResponseDto>> getPersonalChallengeDetail(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = (userDetails != null) ? userDetails.getMemberId() : null;
        PersonalChallengeDetailResponseDto response = readService.getChallengeDetail(memberId, challengeId);
        return ResponseEntity.ok(ApiResponse.success("개인 챌린지 상세 정보를 성공적으로 조회했습니다.", response));
    }
}
