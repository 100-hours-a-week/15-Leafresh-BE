package ktb.leafresh.backend.domain.challenge.group.presentation.controller;

import ktb.leafresh.backend.domain.challenge.group.application.service.EventChallengeReadService;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.EventChallengeResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/events")
public class EventChallengeController {

  private final EventChallengeReadService eventChallengeReadService;

  @GetMapping
  public ResponseEntity<ApiResponse<HashMap<String, List<EventChallengeResponseDto>>>>
      getEventChallenges() {
    List<EventChallengeResponseDto> challenges = eventChallengeReadService.getEventChallenges();
    return ResponseEntity.ok(
        ApiResponse.success(
            "이벤트 챌린지 목록 조회에 성공하였습니다.",
            new HashMap<>() {
              {
                put("eventChallenges", challenges);
              }
            }));
  }
}
