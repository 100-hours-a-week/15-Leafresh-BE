package ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request;

public record AiChatbotFreeTextRequestDto(
        String sessionId,
        String message
) {}
