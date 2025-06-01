package ktb.leafresh.backend.domain.chatbot.infrastructure.client;

import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request.AiChatbotFreeTextRequestDto;
import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.response.AiChatbotResponseDto;

public interface AiChatbotFreeTextClient {
    AiChatbotResponseDto getRecommendation(AiChatbotFreeTextRequestDto requestDto);
}
