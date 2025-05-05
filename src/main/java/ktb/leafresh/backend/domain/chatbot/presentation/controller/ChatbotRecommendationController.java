package ktb.leafresh.backend.domain.chatbot.presentation.controller;

import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.chatbot.application.service.ChatbotRecommendationService;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.request.ChatbotBaseInfoRequestDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.response.ChatbotBaseInfoResponseDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.request.ChatbotFreeTextRequestDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.response.ChatbotFreeTextResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot/recommendation")
@RequiredArgsConstructor
public class ChatbotRecommendationController {

    private final ChatbotRecommendationService recommendationService;

    @PostMapping("/base-info")
    public ResponseEntity<ApiResponse<ChatbotBaseInfoResponseDto>> recommendByBaseInfo(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid ChatbotBaseInfoRequestDto requestDto
    ) {
        ChatbotBaseInfoResponseDto response = recommendationService.recommendByBaseInfo(memberId, requestDto);
        return ResponseEntity.ok(ApiResponse.success("사용자 기본 정보 키워드 선택을 기반으로 챌린지를 추천합니다.", response));
    }

    @PostMapping("/free-text")
    public ResponseEntity<ApiResponse<ChatbotFreeTextResponseDto>> recommendByFreeText(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid ChatbotFreeTextRequestDto requestDto
    ) {
        ChatbotFreeTextResponseDto response = recommendationService.recommendByFreeText(memberId, requestDto);
        return ResponseEntity.ok(ApiResponse.success("사용자 자유 메시지를 기반으로 챌린지를 추천합니다.", response));
    }
}
