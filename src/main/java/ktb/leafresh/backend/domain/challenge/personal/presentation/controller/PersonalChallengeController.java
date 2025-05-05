package ktb.leafresh.backend.domain.challenge.personal.presentation.controller;

import ktb.leafresh.backend.domain.challenge.personal.application.service.PersonalChallengeReadService;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response.PersonalChallengeListResponseDto;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}
