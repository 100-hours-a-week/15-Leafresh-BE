package ktb.leafresh.backend.domain.chatbot.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request.AiChatbotBaseInfoRequestDto;
import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.response.AiChatbotApiResponseDto;
import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.response.AiChatbotResponseDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Profile({"bigbang-prod", "docker-prod"})
public class HttpAiChatbotBaseInfoClient implements AiChatbotBaseInfoClient {

    private final WebClient aiServerWebClient;

    public HttpAiChatbotBaseInfoClient(@Qualifier("textAiWebClient") WebClient aiServerWebClient) {
        this.aiServerWebClient = aiServerWebClient;
    }

    @Override
    public AiChatbotResponseDto getRecommendation(AiChatbotBaseInfoRequestDto requestDto) {
        String rawJson = aiServerWebClient.post()
                .uri("/ai/chatbot/recommendation/base-info")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("[AI 서버 응답 원문 - BaseInfo] \n" + rawJson);

        try {
            // 전용 Wrapper DTO로 파싱
            AiChatbotApiResponseDto parsed =
                    new ObjectMapper().readValue(rawJson, AiChatbotApiResponseDto.class);
            return parsed.data();
        } catch (Exception e) {
            throw new RuntimeException("AI 응답 파싱 실패: " + e.getMessage(), e);
        }
    }
}
