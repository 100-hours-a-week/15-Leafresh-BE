package ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request;

public record AiChatbotBaseInfoRequestDto(
        String conversationId,
        String location,
        String workType,
        String category
) {}
