package ktb.leafresh.backend.domain.chatbot.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import ktb.leafresh.backend.domain.chatbot.application.service.ChatbotRecommendationSseService;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.request.ChatbotBaseInfoRequestDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.request.ChatbotFreeTextRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Chatbot Recommendation SSE", description = "챗봇 추천 실시간 스트리밍 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatbot/recommendation")
@Profile({"docker-local", "eks"})
@Validated
public class ChatbotRecommendationSseController {

  private final ChatbotRecommendationSseService chatbotRecommendationSseService;

  @GetMapping(value = "/base-info", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @Operation(
      summary = "기본 정보 기반 실시간 챗봇 추천",
      description = "사용자의 기본 정보를 기반으로 실시간으로 챌린지를 추천합니다. (SSE)")
  public SseEmitter baseInfo(
      @Parameter(description = "세션 ID", required = true) @RequestParam @NotBlank String sessionId,
      @Parameter(description = "위치", required = true) @RequestParam @NotBlank String location,
      @Parameter(description = "업무 형태", required = true) @RequestParam @NotBlank String workType,
      @Parameter(description = "카테고리", required = true) @RequestParam @NotBlank String category,
      HttpServletResponse response) {

    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("X-Accel-Buffering", "no");

    return chatbotRecommendationSseService.stream(
        "/ai/chatbot/recommendation/base-info",
        new ChatbotBaseInfoRequestDto(sessionId, location, workType, category));
  }

  @GetMapping(value = "/free-text", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @Operation(
      summary = "자유 텍스트 기반 실시간 챗봇 추천",
      description = "사용자의 자유 텍스트를 분석하여 실시간으로 챌린지를 추천합니다. (SSE)")
  public SseEmitter freeText(
      @Parameter(description = "세션 ID", required = true) @RequestParam @NotBlank String sessionId,
      @Parameter(description = "사용자 메시지", required = true) @RequestParam @NotBlank String message,
      HttpServletResponse response) {

    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("X-Accel-Buffering", "no");

    return chatbotRecommendationSseService.stream(
        "/ai/chatbot/recommendation/free-text", new ChatbotFreeTextRequestDto(sessionId, message));
  }
}
