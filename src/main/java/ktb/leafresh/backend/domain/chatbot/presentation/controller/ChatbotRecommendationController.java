package ktb.leafresh.backend.domain.chatbot.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.chatbot.application.service.ChatbotRecommendationService;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.request.ChatbotBaseInfoRequestDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.request.ChatbotFreeTextRequestDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.response.ChatbotRecommendationResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Chatbot Recommendation", description = "챗봇 추천 API")
@RestController
@RequestMapping("/api/chatbot/recommendation")
@RequiredArgsConstructor
@Profile({"bigbang-prod", "docker-prod"})
@Validated
public class ChatbotRecommendationController {

  private final ChatbotRecommendationService recommendationService;

  @PostMapping("/base-info")
  @Operation(summary = "기본 정보 기반 챗봇 추천", description = "사용자의 기본 정보(위치, 업무 형태, 카테고리)를 기반으로 챌린지를 추천합니다.")
  public ResponseEntity<ApiResponse<ChatbotRecommendationResponseDto>> recommendByBaseInfo(
      @Valid @RequestBody ChatbotBaseInfoRequestDto requestDto) {

    ChatbotRecommendationResponseDto response = recommendationService.recommendByBaseInfo(requestDto);

    return ResponseEntity.ok(ApiResponse.success("사용자 기본 정보 키워드 선택을 기반으로 챌린지를 추천합니다.", response));
  }

  @PostMapping("/free-text")
  @Operation(summary = "자유 텍스트 기반 챗봇 추천", description = "사용자의 자유 텍스트 메시지를 분석하여 챌린지를 추천합니다.")
  public ResponseEntity<ApiResponse<ChatbotRecommendationResponseDto>> recommendByFreeText(
      @Valid @RequestBody ChatbotFreeTextRequestDto requestDto) {

    ChatbotRecommendationResponseDto response = recommendationService.recommendByFreeText(requestDto);

    return ResponseEntity.ok(ApiResponse.success("사용자 자유 메시지를 기반으로 챌린지를 추천합니다.", response));
  }
}
