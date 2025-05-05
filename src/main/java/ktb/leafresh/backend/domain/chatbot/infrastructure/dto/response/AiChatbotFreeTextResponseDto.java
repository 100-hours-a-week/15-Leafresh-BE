package ktb.leafresh.backend.domain.chatbot.infrastructure.dto.response;

import java.util.List;

public record AiChatbotFreeTextResponseDto(
        String recommend,
        List<Challenge> challenges
) {
    public record Challenge(String title, String description) {}
}
