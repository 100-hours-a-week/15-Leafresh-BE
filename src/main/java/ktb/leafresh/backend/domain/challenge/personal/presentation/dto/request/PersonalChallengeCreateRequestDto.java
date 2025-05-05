package ktb.leafresh.backend.domain.challenge.personal.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;

@Schema(description = "개인 챌린지 템플릿 생성 요청")
public record PersonalChallengeCreateRequestDto(
        @NotBlank
        @Schema(description = "챌린지 제목")
        String title,

        @NotBlank
        @Schema(description = "챌린지 설명")
        String description,

        @NotNull
        @Schema(description = "요일")
        DayOfWeek dayOfWeek,

        @NotBlank
        @Schema(description = "썸네일 이미지 URL")
        String imageUrl,

        @PositiveOrZero
        @Schema(description = "지급 리워드")
        int leafReward
) {}
