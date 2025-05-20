package ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request;

public record AiChatbotFreeTextRequestDto(
        String conversationId,
        String location,
        String workType,
        String message
) {}
