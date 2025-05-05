package ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request;

public record AiChatbotBaseInfoRequestDto(
        Long memberId,
        String location,
        String workType,
        String category
) {}
