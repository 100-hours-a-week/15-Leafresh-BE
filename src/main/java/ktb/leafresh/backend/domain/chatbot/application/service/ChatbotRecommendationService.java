package ktb.leafresh.backend.domain.chatbot.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatbotRecommendationService {

    private final AiChatbotBaseInfoClient aiChatbotBaseInfoClientClient;
    private final AiChatbotFreeTextClient aiChatbotFreeTextClient;
    ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public ChatbotBaseInfoResponseDto recommendByBaseInfo(ChatbotBaseInfoRequestDto dto) {
        var aiRequest = new AiChatbotBaseInfoRequestDto(dto.location(), dto.workType(), dto.category());
        var aiResponse = aiChatbotBaseInfoClientClient.getRecommendation(aiRequest);

        try {
            String prettyJson = objectMapper.writeValueAsString(aiResponse);
            System.out.println("[AI 응답 - BaseInfo]\n" + prettyJson);
        } catch (Exception e) {
            System.out.println("[AI 응답 파싱 오류] " + e.getMessage());
        }

        List<ChallengeDto> challenges = Optional.ofNullable(aiResponse.challenges())
                .orElse(List.of())
                .stream()
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

    public ChatbotFreeTextResponseDto recommendByFreeText(ChatbotFreeTextRequestDto dto) {
        var aiRequest = new AiChatbotFreeTextRequestDto(dto.location(), dto.workType(), dto.message());
        var aiResponse = aiChatbotFreeTextClient.getRecommendation(aiRequest);

        try {
            String prettyJson = objectMapper.writeValueAsString(aiResponse);
            System.out.println("[AI 응답 - FreeText]\n" + prettyJson);
        } catch (Exception e) {
            System.out.println("[AI 응답 파싱 오류] " + e.getMessage());
        }

        List<ChatbotFreeTextResponseDto.ChallengeDto> challenges = Optional.ofNullable(aiResponse.challenges())
                .orElse(List.of())
                .stream()
                .map(ch -> new ChatbotFreeTextResponseDto.ChallengeDto(ch.title(), ch.description()))
                .toList();

        return ChatbotFreeTextResponseDto.builder()
                .recommend(aiResponse.recommend())
                .challenges(challenges)
                .build();
    }
}
