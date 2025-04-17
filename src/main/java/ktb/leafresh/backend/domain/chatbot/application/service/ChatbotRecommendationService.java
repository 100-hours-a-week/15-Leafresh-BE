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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotRecommendationService {

    private final AiChatbotBaseInfoClient aiChatbotBaseInfoClientClient;
    private final AiChatbotFreeTextClient aiChatbotFreeTextClient;
    ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public ChatbotBaseInfoResponseDto recommendByBaseInfo(ChatbotBaseInfoRequestDto dto) {
        log.info("[챗봇 추천 요청 - BaseInfo] conversationId={}, location={}, workType={}, category={}",
                dto.conversationId(), dto.location(), dto.workType(), dto.category());

        var aiRequest = new AiChatbotBaseInfoRequestDto(
                dto.conversationId(),
                dto.location(),
                dto.workType(),
                dto.category()
        );
        var aiResponse = aiChatbotBaseInfoClientClient.getRecommendation(aiRequest);

        try {
            String prettyJson = objectMapper.writeValueAsString(aiResponse);
            log.info("[AI 응답 - BaseInfo] \n{}", prettyJson);
        } catch (Exception e) {
            log.warn("[AI 응답 파싱 오류 - BaseInfo] {}", e.getMessage());
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
        log.info("[챗봇 추천 요청 - FreeText] conversationId={}, location={}, workType={}, message={}",
                dto.conversationId(), dto.location(), dto.workType(), dto.message());

        var aiRequest = new AiChatbotFreeTextRequestDto(
                dto.conversationId(),
                dto.location(),
                dto.workType(),
                dto.message()
        );
        var aiResponse = aiChatbotFreeTextClient.getRecommendation(aiRequest);

        try {
            String prettyJson = objectMapper.writeValueAsString(aiResponse);
            log.info("[AI 응답 - FreeText] \n{}", prettyJson);
        } catch (Exception e) {
            log.warn("[AI 응답 파싱 오류 - FreeText] {}", e.getMessage());
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
