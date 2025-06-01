package ktb.leafresh.backend.domain.chatbot.infrastructure.client;

import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request.AiChatbotBaseInfoRequestDto;
import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.response.AiChatbotResponseDto;

public interface AiChatbotBaseInfoClient {
    AiChatbotResponseDto getRecommendation(AiChatbotBaseInfoRequestDto requestDto);
}
