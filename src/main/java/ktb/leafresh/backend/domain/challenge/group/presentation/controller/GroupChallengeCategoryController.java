package ktb.leafresh.backend.domain.challenge.group.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ktb.leafresh.backend.domain.challenge.group.application.service.GroupChallengeCategoryService;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeCategoryResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Group Challenge Category", description = "단체 챌린지 카테고리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/group/categories")
public class GroupChallengeCategoryController {

  private final GroupChallengeCategoryService groupChallengeCategoryService;

  @GetMapping
  @Operation(summary = "단체 챌린지 카테고리 목록 조회", description = "사용 가능한 모든 단체 챌린지 카테고리를 조회합니다.")
  public ResponseEntity<ApiResponse<Map<String, List<GroupChallengeCategoryResponseDto>>>> getGroupChallengeCategories() {
    
    List<GroupChallengeCategoryResponseDto> categories = groupChallengeCategoryService.getCategories();
    
    return ResponseEntity.ok(
        ApiResponse.success("단체 챌린지 카테고리 목록 조회에 성공하였습니다.", Map.of("categories", categories)));
  }
}
