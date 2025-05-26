package ktb.leafresh.backend.domain.feedback.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FeedbackRequestDto(

        @NotBlank(message = "요청 사유는 필수입니다.")
        String reason

) {}
