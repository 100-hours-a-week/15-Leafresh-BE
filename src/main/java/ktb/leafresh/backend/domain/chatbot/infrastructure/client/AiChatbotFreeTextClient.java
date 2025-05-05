package ktb.leafresh.backend.domain.chatbot.infrastructure.client;

import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request.AiChatbotFreeTextRequestDto;
import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.response.AiChatbotFreeTextResponseDto;

public interface AiChatbotFreeTextClient {
    AiChatbotFreeTextResponseDto getRecommendation(AiChatbotFreeTextRequestDto requestDto);
}
