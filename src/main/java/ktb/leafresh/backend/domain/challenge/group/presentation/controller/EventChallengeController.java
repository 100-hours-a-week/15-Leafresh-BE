package ktb.leafresh.backend.domain.challenge.group.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ktb.leafresh.backend.domain.challenge.group.application.service.EventChallengeReadService;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.EventChallengeResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Event Challenge", description = "이벤트 챌린지 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/events")
public class EventChallengeController {

  private final EventChallengeReadService eventChallengeReadService;

  @GetMapping
  @Operation(summary = "이벤트 챌린지 목록 조회", description = "현재 진행 중인 이벤트 챌린지 목록을 조회합니다.")
  public ResponseEntity<ApiResponse<Map<String, List<EventChallengeResponseDto>>>>
      getEventChallenges() {

    List<EventChallengeResponseDto> challenges = eventChallengeReadService.getEventChallenges();

    return ResponseEntity.ok(
        ApiResponse.success("이벤트 챌린지 목록 조회에 성공하였습니다.", Map.of("eventChallenges", challenges)));
  }
}
