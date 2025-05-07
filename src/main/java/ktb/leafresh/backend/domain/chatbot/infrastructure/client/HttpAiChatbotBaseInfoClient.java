package ktb.leafresh.backend.domain.chatbot.infrastructure.client;

import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request.AiChatbotBaseInfoRequestDto;
import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.response.AiChatbotBaseInfoResponseDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Profile("!local")
public class HttpAiChatbotBaseInfoClient implements AiChatbotBaseInfoClient {

    private final WebClient aiServerWebClient;

    public HttpAiChatbotBaseInfoClient(
            @Qualifier("aiServerWebClient") WebClient aiServerWebClient
    ) {
        this.aiServerWebClient = aiServerWebClient;
    }

    @Override
    public AiChatbotBaseInfoResponseDto getRecommendation(AiChatbotBaseInfoRequestDto requestDto) {
        return aiServerWebClient.post()
                .uri("/ai/chatbot/recommendation/base-info")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(AiChatbotBaseInfoResponseDto.class)
                .block(); // 동기 호출
    }
}
