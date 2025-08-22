package ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request;

public record AiChatbotBaseInfoRequestDto(
    String sessionId, String location, String workType, String category) {}
