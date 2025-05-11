package ktb.leafresh.backend.domain.chatbot.infrastructure.dto.response;

public record AiChatbotFreeTextApiResponseDto(
        int status,
        String message,
        AiChatbotFreeTextResponseDto data
) {}
