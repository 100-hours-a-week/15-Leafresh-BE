package ktb.leafresh.backend.domain.feedback.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "FeedbackRequest", description = "피드백 생성 요청 DTO")
public record FeedbackRequestDto(
    @Schema(
            description = "피드백 요청 사유",
            example = "지난주 챌린지 활동에 대한 개인 맞춤 피드백을 받고 싶습니다.",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 200)
        @NotBlank(message = "피드백 요청 사유는 필수입니다.")
        String reason) {}
