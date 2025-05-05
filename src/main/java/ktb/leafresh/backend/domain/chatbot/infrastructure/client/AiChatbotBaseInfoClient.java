package ktb.leafresh.backend.domain.chatbot.infrastructure.client;

import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request.AiChatbotBaseInfoRequestDto;
import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.response.AiChatbotBaseInfoResponseDto;

public interface AiChatbotBaseInfoClient {
    AiChatbotBaseInfoResponseDto getRecommendation(AiChatbotBaseInfoRequestDto requestDto);
}
