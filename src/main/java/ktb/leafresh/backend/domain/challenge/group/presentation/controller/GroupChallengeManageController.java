package ktb.leafresh.backend.domain.challenge.group.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import ktb.leafresh.backend.domain.challenge.group.application.service.*;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.enums.GroupChallengeCategoryName;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeCreateRequestDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeUpdateRequestDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.*;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CurrentMemberId;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Group Challenge Management", description = "단체 챌린지 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/group")
@Validated
public class GroupChallengeManageController {

  private final GroupChallengeCreateService groupChallengeCreateService;
  private final GroupChallengeUpdateService groupChallengeUpdateService;
  private final GroupChallengeDeleteService groupChallengeDeleteService;
  private final GroupChallengeSearchReadService searchReadService;
  private final GroupChallengeDetailReadService detailReadService;

  @GetMapping
  @Operation(summary = "단체 챌린지 목록 조회", description = "검색어, 카테고리, 커서 기반 페이지네이션으로 단체 챌린지 목록을 조회합니다.")
  public ResponseEntity<ApiResponse<GroupChallengeListResponseDto>> getGroupChallenges(
      @Parameter(description = "검색어") @RequestParam(required = false) String input,
      @Parameter(description = "챌린지 카테고리") @RequestParam(required = false)
          GroupChallengeCategoryName category,
      @Parameter(description = "커서 ID") @RequestParam(required = false) Long cursorId,
      @Parameter(description = "커서 타임스탬프") @RequestParam(required = false) String cursorTimestamp,
      @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "12") @Min(1) @Max(50)
          int size) {

    CursorPaginationResult<GroupChallengeSummaryResponseDto> result =
        searchReadService.getGroupChallenges(input, category, cursorId, cursorTimestamp, size);

    return ResponseEntity.ok(
        ApiResponse.success("단체 챌린지 목록 조회에 성공하였습니다.", GroupChallengeListResponseDto.from(result)));
  }

  @PostMapping
  @Operation(summary = "단체 챌린지 생성", description = "새로운 단체 챌린지를 생성합니다.")
  public ResponseEntity<ApiResponse<GroupChallengeCreateResponseDto>> createGroupChallenge(
      @CurrentMemberId Long memberId, @Valid @RequestBody GroupChallengeCreateRequestDto request) {

    GroupChallengeCreateResponseDto response =
        groupChallengeCreateService.create(memberId, request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created("단체 챌린지가 생성되었습니다.", response));
  }

  @GetMapping("/{challengeId}")
  @Operation(summary = "단체 챌린지 상세 조회", description = "특정 단체 챌린지의 상세 정보를 조회합니다.")
  public ResponseEntity<ApiResponse<GroupChallengeDetailResponseDto>> getGroupChallengeDetail(
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
      @CurrentMemberId Long memberId) {

    GroupChallengeDetailResponseDto response =
        detailReadService.getChallengeDetail(memberId, challengeId);

    return ResponseEntity.ok(ApiResponse.success("단체 챌린지 상세 정보를 성공적으로 조회했습니다.", response));
  }

  @PatchMapping("/{challengeId}")
  @Operation(summary = "단체 챌린지 수정", description = "기존 단체 챌린지 정보를 수정합니다.")
  public ResponseEntity<ApiResponse<Void>> updateGroupChallenge(
      @CurrentMemberId Long memberId,
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
      @Valid @RequestBody GroupChallengeUpdateRequestDto request) {

    groupChallengeUpdateService.update(memberId, challengeId, request);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @DeleteMapping("/{challengeId}")
  @Operation(summary = "단체 챌린지 삭제", description = "기존 단체 챌린지를 삭제합니다.")
  public ResponseEntity<ApiResponse<Map<String, Long>>> deleteGroupChallenge(
      @CurrentMemberId Long memberId,
      @Parameter(description = "챌린지 ID") @PathVariable Long challengeId) {

    Long deletedId = groupChallengeDeleteService.delete(memberId, challengeId);

    return ResponseEntity.ok(
        ApiResponse.success("단체 챌린지가 성공적으로 삭제되었습니다.", Map.of("deletedChallengeId", deletedId)));
  }
}
