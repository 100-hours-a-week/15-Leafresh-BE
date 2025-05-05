package ktb.leafresh.backend.domain.challenge.personal.presentation.controller;

import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.challenge.personal.application.service.PersonalChallengeCreateService;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.request.PersonalChallengeCreateRequestDto;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response.PersonalChallengeCreateResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/challenges/personal")
public class PersonalChallengeController {

    private final PersonalChallengeCreateService createService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PersonalChallengeCreateResponseDto>> create(
            @Valid @RequestBody PersonalChallengeCreateRequestDto request
    ) {
        PersonalChallengeCreateResponseDto response = createService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("개인 챌린지 템플릿이 성공적으로 생성되었습니다.", response));
    }
}
