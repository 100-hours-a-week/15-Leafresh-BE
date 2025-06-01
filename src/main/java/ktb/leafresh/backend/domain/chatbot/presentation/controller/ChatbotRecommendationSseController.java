package ktb.leafresh.backend.domain.chatbot.presentation.controller;

import ktb.leafresh.backend.domain.chatbot.application.service.ChatbotRecommendationSseService;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.request.ChatbotBaseInfoRequestDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.request.ChatbotFreeTextRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatbot/recommendation")
public class ChatbotRecommendationSseController {

    private final ChatbotRecommendationSseService chatbotRecommendationSseService;

    @GetMapping(value = "/base-info", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter baseInfo(
            @RequestParam String sessionId,
            @RequestParam String location,
            @RequestParam String workType,
            @RequestParam String category
    ) {
        return chatbotRecommendationSseService.stream(
                "/ai/chatbot/recommendation/base-info",
                new ChatbotBaseInfoRequestDto(sessionId, location, workType, category)
        );
    }

    @GetMapping(value = "/free-text", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter freeText(
            @RequestParam String sessionId,
            @RequestParam String message
    ) {
        return chatbotRecommendationSseService.stream(
                "/ai/chatbot/recommendation/free-text",
                new ChatbotFreeTextRequestDto(sessionId, message)
        );
    }
}
