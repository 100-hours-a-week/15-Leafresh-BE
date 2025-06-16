package ktb.leafresh.backend.domain.chatbot.presentation.controller;

import ktb.leafresh.backend.domain.chatbot.application.service.ChatbotRecommendationSseService;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.request.ChatbotBaseInfoRequestDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.request.ChatbotFreeTextRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatbot/recommendation")
@Profile({"docker-local", "docker-prod"})
public class ChatbotRecommendationSseController {

    private final ChatbotRecommendationSseService chatbotRecommendationSseService;

    @GetMapping(value = "/base-info", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> baseInfo(
            @RequestParam String sessionId,
            @RequestParam String location,
            @RequestParam String workType,
            @RequestParam String category,
            ServerHttpResponse response
    ) {
        response.getHeaders().add("Cache-Control", "no-cache");
        response.getHeaders().add("X-Accel-Buffering", "no");

        return chatbotRecommendationSseService.streamFlux(
                "/ai/chatbot/recommendation/base-info",
                new ChatbotBaseInfoRequestDto(sessionId, location, workType, category)
        );
    }

    @GetMapping(value = "/free-text", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> freeText(
            @RequestParam String sessionId,
            @RequestParam String message,
            ServerHttpResponse response
    ) {
        response.getHeaders().add("Cache-Control", "no-cache");
        response.getHeaders().add("X-Accel-Buffering", "no");

        return chatbotRecommendationSseService.streamFlux(
                "/ai/chatbot/recommendation/free-text",
                new ChatbotFreeTextRequestDto(sessionId, message)
        );
    }
}
