package ktb.leafresh.backend.domain.feedback.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FeedbackResultRequestDto(
        @NotNull(message = "memberId는 필수 항목입니다.")
        Long memberId,

        @NotBlank(message = "feedback는 필수 항목입니다.")
        String content
) {}
