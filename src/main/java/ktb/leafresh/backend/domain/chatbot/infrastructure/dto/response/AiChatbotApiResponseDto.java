package ktb.leafresh.backend.domain.chatbot.infrastructure.dto.response;

public record AiChatbotApiResponseDto(
        int status,
        String message,
        AiChatbotResponseDto data
) {}
