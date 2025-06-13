package ktb.leafresh.backend.domain.chatbot.presentation.controller;

import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.chatbot.application.service.ChatbotRecommendationService;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.request.ChatbotBaseInfoRequestDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.request.ChatbotFreeTextRequestDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.response.ChatbotRecommendationResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/chatbot/recommendation")
@RequiredArgsConstructor
@Profile("bigbang-prod")
public class ChatbotRecommendationController {

    private final ChatbotRecommendationService recommendationService;

    @PostMapping("/base-info")
    public ResponseEntity<ApiResponse<ChatbotRecommendationResponseDto>> recommendByBaseInfo(
            @RequestBody @Valid ChatbotBaseInfoRequestDto requestDto
    ) {
        log.info("[챗봇 추천 요청 - 기본 정보] sessionId={}, location={}, workType={}, category={}",
                requestDto.sessionId(), requestDto.location(), requestDto.workType(), requestDto.category());

        ChatbotRecommendationResponseDto response = recommendationService.recommendByBaseInfo(requestDto);
        log.info("[챗봇 추천 완료 - 기본 정보] 추천 결과: {}", response.recommend());

        return ResponseEntity.ok(ApiResponse.success("사용자 기본 정보 키워드 선택을 기반으로 챌린지를 추천합니다.", response));
    }

    @PostMapping("/free-text")
    public ResponseEntity<ApiResponse<ChatbotRecommendationResponseDto>> recommendByFreeText(
            @RequestBody @Valid ChatbotFreeTextRequestDto requestDto
    ) {
        log.info("[챗봇 추천 요청 - 자유 텍스트] sessionId={}, message={}",
                requestDto.sessionId(), requestDto.message());

        ChatbotRecommendationResponseDto response = recommendationService.recommendByFreeText(requestDto);
        log.info("[챗봇 추천 완료 - 자유 텍스트] 추천 결과: {}", response.recommend());

        return ResponseEntity.ok(ApiResponse.success("사용자 자유 메시지를 기반으로 챌린지를 추천합니다.", response));
    }
}
