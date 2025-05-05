package ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request;

public record AiChatbotFreeTextRequestDto(
        Long memberId,
        String location,
        String workType,
        String message
) {}
