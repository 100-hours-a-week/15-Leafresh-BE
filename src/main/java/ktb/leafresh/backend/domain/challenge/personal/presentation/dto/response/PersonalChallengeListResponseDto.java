package ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "개인 챌린지 목록 응답")
@Builder
public record PersonalChallengeListResponseDto(
    @Schema(description = "개인 챌린지 목록") List<PersonalChallengeSummaryDto> personalChallenges) {}
