package ktb.leafresh.backend.domain.chatbot.application.service;

import ktb.leafresh.backend.domain.chatbot.infrastructure.client.AiChatbotBaseInfoClient;
import ktb.leafresh.backend.domain.chatbot.infrastructure.client.AiChatbotFreeTextClient;
import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request.AiChatbotBaseInfoRequestDto;
import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request.AiChatbotFreeTextRequestDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.request.ChatbotBaseInfoRequestDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.request.ChatbotFreeTextRequestDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.response.ChatbotBaseInfoResponseDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.response.ChatbotBaseInfoResponseDto.ChallengeDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.response.ChatbotFreeTextResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatbotRecommendationService {

    private final AiChatbotBaseInfoClient aiChatbotBaseInfoClientClient;
    private final AiChatbotFreeTextClient aiChatbotFreeTextClient;

    public ChatbotBaseInfoResponseDto recommendByBaseInfo(Long memberId, ChatbotBaseInfoRequestDto dto) {
        var aiRequest = new AiChatbotBaseInfoRequestDto(memberId, dto.location(), dto.workType(), dto.category());
        var aiResponse = aiChatbotBaseInfoClientClient.getRecommendation(aiRequest);

        List<ChallengeDto> challenges = aiResponse.challenges().stream()
                .map(ch -> ChallengeDto.builder()
                        .title(ch.title())
                        .description(ch.description())
                        .build())
                .toList();

        return ChatbotBaseInfoResponseDto.builder()
                .recommend(aiResponse.recommend())
                .challenges(challenges)
                .build();
    }

    public ChatbotFreeTextResponseDto recommendByFreeText(Long memberId, ChatbotFreeTextRequestDto dto) {
        var aiRequest = new AiChatbotFreeTextRequestDto(memberId, dto.location(), dto.workType(), dto.message());
        var aiResponse = aiChatbotFreeTextClient.getRecommendation(aiRequest);

        var challenges = aiResponse.challenges().stream()
                .map(ch -> new ChatbotFreeTextResponseDto.ChallengeDto(ch.title(), ch.description()))
                .toList();

        return ChatbotFreeTextResponseDto.builder()
                .recommend(aiResponse.recommend())
                .challenges(challenges)
                .build();
    }
}
