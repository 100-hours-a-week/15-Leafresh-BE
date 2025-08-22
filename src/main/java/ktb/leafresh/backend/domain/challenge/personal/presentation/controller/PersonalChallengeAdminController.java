package ktb.leafresh.backend.domain.challenge.personal.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.challenge.personal.application.service.PersonalChallengeCreateService;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.request.PersonalChallengeCreateRequestDto;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response.PersonalChallengeCreateResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Personal Challenge Admin", description = "개인 챌린지 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/challenges/personal")
@Validated
public class PersonalChallengeAdminController {

  private final PersonalChallengeCreateService createService;

  @PostMapping
  @Operation(summary = "개인 챌린지 생성", description = "새로운 개인 챌린지를 생성합니다. (관리자 권한 필요)")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<PersonalChallengeCreateResponseDto>> create(
      @Valid @RequestBody PersonalChallengeCreateRequestDto request) {
    
    PersonalChallengeCreateResponseDto response = createService.create(request);
    
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created("개인 챌린지가 성공적으로 생성되었습니다.", response));
  }
}
