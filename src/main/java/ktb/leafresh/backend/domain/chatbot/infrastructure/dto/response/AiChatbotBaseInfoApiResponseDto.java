package ktb.leafresh.backend.domain.chatbot.infrastructure.dto.response;

public record AiChatbotBaseInfoApiResponseDto(
        int status,
        String message,
        AiChatbotBaseInfoResponseDto data
) {}
