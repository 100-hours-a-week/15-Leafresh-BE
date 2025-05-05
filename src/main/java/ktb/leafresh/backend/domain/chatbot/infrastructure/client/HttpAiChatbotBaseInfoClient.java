package ktb.leafresh.backend.domain.chatbot.infrastructure.client;

import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.request.AiChatbotBaseInfoRequestDto;
import ktb.leafresh.backend.domain.chatbot.infrastructure.dto.response.AiChatbotBaseInfoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Profile("!local")
@RequiredArgsConstructor
public class HttpAiChatbotBaseInfoClient implements AiChatbotBaseInfoClient {

    private final WebClient aiServerWebClient;

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
