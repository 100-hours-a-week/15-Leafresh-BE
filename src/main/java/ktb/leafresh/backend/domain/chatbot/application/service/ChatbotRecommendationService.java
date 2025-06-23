package ktb.leafresh.backend.domain.chatbot.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import ktb.leafresh.backend.domain.chatbot.infrastructure.client.AiChatbotBaseInfoClient;
import ktb.leafresh.backend.domain.chatbot.infrastructure.client.AiChatbotFreeTextClient;
import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request.AiChatbotBaseInfoRequestDto;
import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request.AiChatbotFreeTextRequestDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.request.ChatbotBaseInfoRequestDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.request.ChatbotFreeTextRequestDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.response.ChatbotRecommendationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"bigbang-prod", "docker-local", "docker-prod"})
public class ChatbotRecommendationService {

    private final AiChatbotBaseInfoClient aiChatbotBaseInfoClientClient;
    private final AiChatbotFreeTextClient aiChatbotFreeTextClient;
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public ChatbotRecommendationResponseDto recommendByBaseInfo(ChatbotBaseInfoRequestDto dto) {
        log.info("[챗봇 추천 요청 - BaseInfo] sessionId={}, location={}, workType={}, category={}",
                dto.sessionId(), dto.location(), dto.workType(), dto.category());

        var aiRequest = new AiChatbotBaseInfoRequestDto(
                dto.sessionId(),
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

        List<ChatbotRecommendationResponseDto.ChallengeDto> challenges =
                Optional.ofNullable(aiResponse.challenges())
                        .orElse(List.of())
                        .stream()
                        .map(ch -> ChatbotRecommendationResponseDto.ChallengeDto.builder()
                                .title(ch.title())
                                .description(ch.description())
                                .category(ch.category())
                                .label(ch.label())
                                .build())
                        .toList();

        return ChatbotRecommendationResponseDto.builder()
                .recommend(aiResponse.recommend())
                .challenges(challenges)
                .build();
    }

    public ChatbotRecommendationResponseDto recommendByFreeText(ChatbotFreeTextRequestDto dto) {
        log.info("[챗봇 추천 요청 - FreeText] sessionId={}, message={}",
                dto.sessionId(), dto.message());

        var aiRequest = new AiChatbotFreeTextRequestDto(
                dto.sessionId(),
                dto.message()
        );
        var aiResponse = aiChatbotFreeTextClient.getRecommendation(aiRequest);

        try {
            String prettyJson = objectMapper.writeValueAsString(aiResponse);
            log.info("[AI 응답 - FreeText] \n{}", prettyJson);
        } catch (Exception e) {
            log.warn("[AI 응답 파싱 오류 - FreeText] {}", e.getMessage());
        }

        List<ChatbotRecommendationResponseDto.ChallengeDto> challenges =
                Optional.ofNullable(aiResponse.challenges())
                        .orElse(List.of())
                        .stream()
                        .map(ch -> ChatbotRecommendationResponseDto.ChallengeDto.builder()
                                .title(ch.title())
                                .description(ch.description())
                                .category(ch.category())
                                .label(ch.label())
                                .build())
                        .toList();

        return ChatbotRecommendationResponseDto.builder()
                .recommend(aiResponse.recommend())
                .challenges(challenges)
                .build();
    }
}
