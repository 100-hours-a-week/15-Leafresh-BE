package ktb.leafresh.backend.domain.chatbot.presentation.controller;

import ktb.leafresh.backend.domain.chatbot.application.service.ChatbotRecommendationSseService;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.request.ChatbotBaseInfoRequestDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.request.ChatbotFreeTextRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatbot/recommendation")
@Profile({"docker-local", "eks"})
public class ChatbotRecommendationSseController {

  private final ChatbotRecommendationSseService chatbotRecommendationSseService;

  @GetMapping(value = "/base-info", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter baseInfo(
      @RequestParam String sessionId,
      @RequestParam String location,
      @RequestParam String workType,
      @RequestParam String category,
      HttpServletResponse response) {
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("X-Accel-Buffering", "no");

    return chatbotRecommendationSseService.stream(
        "/ai/chatbot/recommendation/base-info",
        new ChatbotBaseInfoRequestDto(sessionId, location, workType, category));
  }

  @GetMapping(value = "/free-text", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter freeText(
      @RequestParam String sessionId, @RequestParam String message, HttpServletResponse response) {
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("X-Accel-Buffering", "no");

    return chatbotRecommendationSseService.stream(
        "/ai/chatbot/recommendation/free-text", new ChatbotFreeTextRequestDto(sessionId, message));
  }
}
