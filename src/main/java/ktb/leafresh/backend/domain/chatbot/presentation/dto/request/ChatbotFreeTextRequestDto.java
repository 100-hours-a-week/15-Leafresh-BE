package ktb.leafresh.backend.domain.chatbot.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChatbotFreeTextRequestDto(
        @NotBlank(message = "location은 필수입니다.") String location,
        @NotBlank(message = "workType은 필수입니다.") String workType,
        @NotBlank(message = "message는 필수입니다.") String message
) {}
