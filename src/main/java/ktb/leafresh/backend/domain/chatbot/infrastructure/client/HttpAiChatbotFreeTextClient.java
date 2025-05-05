package ktb.leafresh.backend.domain.chatbot.infrastructure.client;

import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request.AiChatbotFreeTextRequestDto;
import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.response.AiChatbotFreeTextResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Profile("!local")
@RequiredArgsConstructor
public class HttpAiChatbotFreeTextClient implements AiChatbotFreeTextClient {

    private final WebClient aiServerWebClient;

    @Override
    public AiChatbotFreeTextResponseDto getRecommendation(AiChatbotFreeTextRequestDto requestDto) {
        return aiServerWebClient.post()
                .uri("/ai/chatbot/recommendation/free-text")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(AiChatbotFreeTextResponseDto.class)
                .block();
    }
}
