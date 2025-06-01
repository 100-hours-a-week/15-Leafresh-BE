package ktb.leafresh.backend.domain.chatbot.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatbotFreeTextRequestDto(
        @NotNull(message = "sessionId는 필수입니다.") String sessionId,
        @NotBlank(message = "message는 필수입니다.") String message
) {}
