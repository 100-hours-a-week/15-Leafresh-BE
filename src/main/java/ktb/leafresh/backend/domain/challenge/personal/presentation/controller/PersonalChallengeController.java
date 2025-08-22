package ktb.leafresh.backend.domain.challenge.personal.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.challenge.personal.application.service.PersonalChallengeReadService;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response.PersonalChallengeDetailResponseDto;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response.PersonalChallengeListResponseDto;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response.PersonalChallengeRuleResponseDto;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Personal Challenge", description = "개인 챌린지 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/personal")
@Validated
public class PersonalChallengeController {

  private final PersonalChallengeReadService readService;

  @GetMapping
  @Operation(summary = "개인 챌린지 목록 조회", description = "요일별 개인 챌린지 목록을 조회합니다.")
  public ResponseEntity<ApiResponse<PersonalChallengeListResponseDto>> getPersonalChallengesByDay(
      @Parameter(description = "요일", required = true) @RequestParam @Valid DayOfWeek dayOfWeek) {
    
    PersonalChallengeListResponseDto response = readService.getByDayOfWeek(dayOfWeek);
    return ResponseEntity.ok(ApiResponse.success("개인챌린지 목록 조회에 성공하였습니다.", response));
  }

  @GetMapping("/{challengeId}")
  @Operation(summary = "개인 챌린지 상세 조회", description = "특정 개인 챌린지의 상세 정보를 조회합니다. 로그인한 사용자에게는 추가 정보를 제공합니다.")
  public ResponseEntity<ApiResponse<PersonalChallengeDetailResponseDto>> getPersonalChallengeDetail(
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId, 
      @CurrentMemberId Long memberId) {
    
    PersonalChallengeDetailResponseDto response = readService.getChallengeDetail(memberId, challengeId);
    return ResponseEntity.ok(ApiResponse.success("개인 챌린지 상세 정보를 성공적으로 조회했습니다.", response));
  }

  @GetMapping("/{challengeId}/rules")
  @Operation(summary = "개인 챌린지 인증 규약 조회", description = "개인 챌린지의 인증 규약 정보를 조회합니다.")
  public ResponseEntity<ApiResponse<PersonalChallengeRuleResponseDto>> getPersonalChallengeRules(
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId) {

    PersonalChallengeRuleResponseDto response = readService.getChallengeRules(challengeId);
    return ResponseEntity.ok(ApiResponse.success("개인 챌린지 인증 규약 정보를 성공적으로 조회했습니다.", response));
  }
}
